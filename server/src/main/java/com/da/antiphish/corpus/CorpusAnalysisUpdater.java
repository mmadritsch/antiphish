package com.da.antiphish.corpus;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.lists.ListsHandler;
import com.da.antiphish.tasks.DomainAgeTask;
import com.da.antiphish.tasks.DotsTask;
import com.da.antiphish.tasks.EmptyLinksTask;
import com.da.antiphish.tasks.FormTagsTask;
import com.da.antiphish.tasks.FrameTask;
import com.da.antiphish.tasks.IPAddressTask;
import com.da.antiphish.tasks.InputTagsTask;
import com.da.antiphish.tasks.MetaTagsTask;
import com.da.antiphish.tasks.NoLinksBodyTask;
import com.da.antiphish.tasks.NonmatchingUrlTask;
import com.da.antiphish.tasks.OnlyScriptTask;
import com.da.antiphish.tasks.PlaintextContentTask;
import com.da.antiphish.tasks.PunctuationTask;
import com.da.antiphish.tasks.SuspiciousLinksTask;
import com.da.antiphish.tasks.SuspiciousScriptContentTask;
import com.da.antiphish.tasks.SuspiciousUrlTask;
import com.da.antiphish.tasks.Task;
import com.da.antiphish.tasks.TaskResult;
import com.da.antiphish.tasks.TitleTask;
import com.da.antiphish.tasks.WebsiteIdentityTask;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * CorpusAnalysisUpdater analyses the specified corpus with the specified task and updates the specified corpus 
 * analysis JSON files accordingly.
 * 
 * @author Marco Madritsch
 */
public class CorpusAnalysisUpdater {
	private final static Logger LOGGER = LoggerFactory.getLogger(CorpusAnalysisUpdater.class);
	private static final String testFilesPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/test";
	private static final String testAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/test/test-analysis_2017-12-10.json";
	private static final String trainingFilesPath = "C:/Users/MarcoM/Documents/Corpus/2017-11-29_new/training";
	private static final String trainingAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_2017-12-10.json";
	private static final ListsHandler listsHandler = new ListsHandler();
	
	/**
	 * Reads first all training corpus analysis results out of the specified json file (path in variable 
	 * trainingAnalysisPath) and stores them in a list of CorpusAnalysisEntry objects. Analyses afterwards all files
	 * (websites) of the specified training corpus (path in variable trainingFilesPath) with the specified task, 
	 * updates for each file (website) the corresponding entry in the list of CorpusAnalysisEntry objects and stores 
	 * the new  analysis results in a new json file (path in variable trainingStoragePath). Repeats then this procedure 
	 * also for the test corpus.
	 */
	public static void main(String[] args) {
		// set log level to info --> less output, faster computation
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
        
		// read training corpus analysis results from file
		List<CorpusAnalysisEntry> trainingAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(trainingAnalysisPath));
				
        // update training analysis
        File trainingPhishingDirectory = new File(trainingFilesPath + "/phishing");
		File trainingLegitimateDirectory = new File(trainingFilesPath + "/legitimate");
		File[] trainingFiles = ArrayUtils.addAll(trainingPhishingDirectory.listFiles(), trainingLegitimateDirectory.listFiles());
		String trainingStoragePath = trainingFilesPath + "/training-analysis_new.json";
		
		updateAnalysis(trainingFiles, trainingAnalysisList, trainingStoragePath, PunctuationTask.class.getSimpleName());
		
		// read test corpus analysis results from file
		List<CorpusAnalysisEntry> testAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(testAnalysisPath));
				
		// update test corpus
        File testPhishingDirectory = new File(testFilesPath + "/phishing");
		File testLegitimateDirectory = new File(testFilesPath + "/legitimate");
		File[] testFiles = ArrayUtils.addAll(testPhishingDirectory.listFiles(), testLegitimateDirectory.listFiles());
		String testStoragePath = testFilesPath + "/test-analysis_new.json";
		
		updateAnalysis(testFiles, testAnalysisList, testStoragePath, PunctuationTask.class.getSimpleName());
        
	}
	
	/**
	 * Iterates over all files (websites) of the given array files, analyses them with the specified task (variable
	 * taskName) and updates the result in the given list analysisEntryList. Stores the updated list of 
	 * CorpusAnalysisEntry objects under the path specified in variable storagePath.
	 * @param files				the array of all files (websites) to be analysed
	 * @param analysisEntryList	the list of all CorpusAnalysisEntry objects
	 * @param storagePath		the path in which the new json file of the analysis results should be stored
	 * @param taskName			the name of the task that should be used for the analysis
	 */
	public static void updateAnalysis(File[] files, List<CorpusAnalysisEntry> analysisEntryList, String storagePath, 
			String taskName) {
		// counts
		int total = 0;		// number of total analysed files (websites)
		int success = 0;	// number of successfully analysed files (websites)
		int fail = 0;		// number of failed analysed files (websites)
				
		try {
        	// json generator to store results object by object into json file
        	JsonFactory jfactory = new JsonFactory();
			JsonGenerator jGenerator = jfactory.createGenerator(new File(storagePath), JsonEncoding.UTF8);
			jGenerator.setCodec(new ObjectMapper());
			jGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			jGenerator.writeStartArray();
			
			// TaskResult objects of a single analysis
	 		TaskResult taskResult;
	 		
	 		// node for each website which will be read from json
      		JsonNode websiteNode;
     		
      		// corpus analysis entry object
      		CorpusAnalysisEntry corpusAnalysisEntry;
      		List<CorpusAnalysisEntry> matchingCorpusAnalysisEntries;
      		
     		// hashmap for storing task result
     		Map<String, Double> taskResultInnerMap;
     		
	 		// read each website from file and analyse it
	        for(File file : files) {
	        	total++;
	        	
	        	// read website from file
	        	websiteNode = CorpusHelper.readWebsiteFromFile(file);
	        	final String url = websiteNode.path("url").asText();
	            
	            // analyse website with specific task
	            taskResult = analyseWebsite(websiteNode, taskName);
	            
	            // check task results for null
	            if(taskResult != null) {
	            	success++;
	            	LOGGER.info("Analysed website successfully: " + success);
	            	
	            	// get CorpusAnalysisEntry object of that website and update it
	            	matchingCorpusAnalysisEntries = analysisEntryList.stream().filter(entry -> entry.getUrl().equals(url)).collect(Collectors.toList());
		            if(matchingCorpusAnalysisEntries.size() > 1 || matchingCorpusAnalysisEntries.size() == 0) {
		            	LOGGER.warn("Found " + matchingCorpusAnalysisEntries.size() + " entries for url " + websiteNode.path("url").asText());
		            } else {
		            	corpusAnalysisEntry = matchingCorpusAnalysisEntries.get(0);
			            
		            	// update hashmap of task results
	            		taskResultInnerMap = new HashMap<String, Double>();
	            		taskResultInnerMap.put("score", taskResult.getScore());
	            		taskResultInnerMap.put("addInfo", taskResult.getAddInfo());
	            		corpusAnalysisEntry.getTaskResults().put(taskResult.getTaskName(), taskResultInnerMap);
		            	
		            	// write object to new file
		            	jGenerator.writeObject(corpusAnalysisEntry);
		            }
	            } else {
	            	fail++;
	            	LOGGER.warn("Failed to analyse website: " + fail);
	            }
	        }
	        
	        jGenerator.writeEndArray();
	        jGenerator.close();
	        
	        LOGGER.debug("Analysed " + total + " websites, " + success + " successful, " + fail + " failed");
		} catch (IOException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
	}
	
	/**
	 * Analyses a single website with the given task.
	 * @param node		the json node of the website
	 * @param taskName	the name of the task that should be used for the analysis
	 * @return			the TaskResult object of the analysis
	 */
	public static TaskResult analyseWebsite(JsonNode node, String taskName) {
		URL url;
		try {
			url = new URL(node.path("url").asText());
		} catch (MalformedURLException e) {
			return null;
		}
		
		// get html content
		Document htmlDoc = Jsoup.parse(node.path("content").asText());
		
		// check if content is null
		if(htmlDoc == null) {
			return null;
		}
        
        // create specific task 
		// init right task
		Task task = null;
		if(taskName.equals(DomainAgeTask.class.getSimpleName())) {
			task = new DomainAgeTask(url);
		} else if(taskName.equals(DotsTask.class.getSimpleName())) {
			task = new DotsTask(url);
		} else if(taskName.equals(EmptyLinksTask.class.getSimpleName())) {
			task = new EmptyLinksTask(htmlDoc);
		} else if(taskName.equals(FormTagsTask.class.getSimpleName())) {
			task = new FormTagsTask(htmlDoc, url, listsHandler.getFormlist());
		} else if(taskName.equals(FrameTask.class.getSimpleName())) {
			task = new FrameTask(htmlDoc, url);
		} else if(taskName.equals(InputTagsTask.class.getSimpleName())) {
			task = new InputTagsTask(htmlDoc);
		} else if(taskName.equals(IPAddressTask.class.getSimpleName())) {
			task = new IPAddressTask(url);
		} else if(taskName.equals(MetaTagsTask.class.getSimpleName())) {
			task = new MetaTagsTask(htmlDoc, listsHandler.getMetalist());
		} else if(taskName.equals(NoLinksBodyTask.class.getSimpleName())) {
			task = new NoLinksBodyTask(htmlDoc);
		} else if(taskName.equals(NonmatchingUrlTask.class.getSimpleName())) {
			task = new NonmatchingUrlTask(htmlDoc);
		} else if(taskName.equals(OnlyScriptTask.class.getSimpleName())) {
			task = new OnlyScriptTask(htmlDoc);
		} else if(taskName.equals(PunctuationTask.class.getSimpleName())) {
			task = new PunctuationTask(url);
		} else if(taskName.equals(PlaintextContentTask.class.getSimpleName())) {
			task = new PlaintextContentTask(htmlDoc, listsHandler.getContentlist());
		} else if(taskName.equals(SuspiciousLinksTask.class.getSimpleName())) {
			task = new SuspiciousLinksTask(htmlDoc);
		} else if(taskName.equals(SuspiciousScriptContentTask.class.getSimpleName())) {
			task = new SuspiciousScriptContentTask(htmlDoc, listsHandler.getScriptlist());
		} else if(taskName.equals(SuspiciousUrlTask.class.getSimpleName())) {
			task = new SuspiciousUrlTask(url);
		} else if(taskName.equals(TitleTask.class.getSimpleName())) {
			task = new TitleTask(htmlDoc, url);
		} else if(taskName.equals(WebsiteIdentityTask.class.getSimpleName())) {
			task = new WebsiteIdentityTask(htmlDoc, url);
		}
        
        // TaskResult object
        TaskResult taskResult;
        
        if(task != null) {
	        try {
	        	// invoke task
	        	taskResult = task.call();
		        
	        	// return result
		        return taskResult;
		        
	        } catch (Exception e) {
	        	LOGGER.warn(e.getClass().getSimpleName() + " while invoking task");
	            return null;
	        }
        } else {
        	return null;
        }
	}
}
