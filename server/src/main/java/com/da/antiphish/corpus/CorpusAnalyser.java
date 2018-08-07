package com.da.antiphish.corpus;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.configuration.ConfigurationHandler;
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
import com.da.antiphish.tasks.PunctuationTask;
import com.da.antiphish.tasks.PlaintextContentTask;
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
 * CorpusAnalyser analyses a corpus of legitimate and phishing websites and stores for each analysed website all task 
 * results in a JSON file.
 * 
 * @author Marco Madritsch
 */
public class CorpusAnalyser {
	private static final Logger LOGGER = LoggerFactory.getLogger(CorpusAnalyser.class);
	private static final String testPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/test";
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final ListsHandler listsHandler = new ListsHandler();
	private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();
	private static final ExecutorService executor = Executors.newFixedThreadPool(8);
	
	/**
	 * Analyses all websites from a training corpus (path in variable trainingPath) and all websites from a test
	 * corpus (path in variable testPath) and stores all analysis results of each corpus in an own json file.
	 */
	public static void main(String[] args) {
		// Logger log level
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("com.da.antiphish");
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// set task thresholds based on config values
		DomainAgeTask.setDomainAgeThreshold(configurationHandler.getConfiguration().getDomainAgeThreshold());
		DotsTask.setDotsThreshold(configurationHandler.getConfiguration().getDotsThreshold());
		EmptyLinksTask.setEmptyLinksThreshold(configurationHandler.getConfiguration().getEmptyLinksThreshold());
		PunctuationTask.setPunctuationThreshold(configurationHandler.getConfiguration().getPunctuationThreshold());
        
        // analyse training corpus
        File trainingPhishingDirectory = new File(trainingPath + "/phishing");
		File trainingLegitimateDirectory = new File(trainingPath + "/legitimate");
		File[] trainingFiles = ArrayUtils.addAll(trainingPhishingDirectory.listFiles(), 
				trainingLegitimateDirectory.listFiles());
		String trainingStoragePath = trainingPath + "/training-analysis.json";
		
		analyseFiles(trainingFiles, trainingStoragePath);
		
		// analyse test corpus
        File testPhishingDirectory = new File(testPath + "/phishing");
		File testLegitimateDirectory = new File(testPath + "/legitimate");
		File[] testFiles = ArrayUtils.addAll(testPhishingDirectory.listFiles(), testLegitimateDirectory.listFiles());
		String testStoragePath = testPath + "/test-analysis.json";
		
		analyseFiles(testFiles, testStoragePath);
        
	}
	
	/**
	 * Reads each website from the given array of files, analyses it and stores the results in a json file.
	 * @param files			the array of all files (websites) to be analysed
	 * @param storagePath	the path in which the json file should be stored
	 */
	public static void analyseFiles(File[] files, String storagePath) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		try {
        	// json generator to store results object by object into json file
        	JsonFactory jfactory = new JsonFactory();
			JsonGenerator jGenerator = jfactory.createGenerator(new File(storagePath), JsonEncoding.UTF8);
			jGenerator.setCodec(new ObjectMapper());
			jGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			jGenerator.writeStartArray();
			
			// list of all TaskResult objects of a single analysis
	 		List<TaskResult> taskResults;
	 		
	 		// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// CorpusAnalysisEntry object for storing result of an analysis
     		CorpusAnalysisEntry corpusAnalysisEntry;
     		
     		// hashmap for storing task results
     		Map<String, Map<String, Double>> taskResultsMap;
     		
	 		// read each website from file and analyse it
	        for(File file : files) {
	        	total++;
	        	
	        	// read website from file
	        	websiteNode = CorpusHelper.readWebsiteFromFile(file);
	            
	            // analyse website
	            taskResults = analyseWebsite(websiteNode);
	            
	            // check task results for null
	            if(taskResults != null) {
	            	success++;
	            	LOGGER.info("Analysed website successfully: " + success);
	            	
	            	// create new CorpusAnalysisEntry object and fill it
		            corpusAnalysisEntry = new CorpusAnalysisEntry();
		            corpusAnalysisEntry.setUrl(websiteNode.path("url").asText());
		            
	            	// iterate over each TaskResult and store it in hash map
		            taskResultsMap = new HashMap<String, Map<String, Double>>();
		            Map<String, Double> taskResultInnerMap;
	            	for(TaskResult taskResult : taskResults) {
	            		taskResultInnerMap = new HashMap<String, Double>();
	            		taskResultInnerMap.put("score", taskResult.getScore());
	            		taskResultInnerMap.put("addInfo", taskResult.getAddInfo());
	            		taskResultsMap.put(taskResult.getTaskName(), taskResultInnerMap);
	            	}
	            	
	            	// set task results hash map
	            	corpusAnalysisEntry.setTaskResults(taskResultsMap);
	            	
	            	// store if it is a phishing website or not
	            	if(file.getName().contains("phishing")) {
	            		corpusAnalysisEntry.setPhishing(true);
	            	} else {
	            		corpusAnalysisEntry.setPhishing(false);
	            	}
	            	
	            	// write object to file
	            	jGenerator.writeObject(corpusAnalysisEntry);
	            	
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
	 * Analyses a single website with all analysis tasks (except BlacklistTask and WhitelistTask).
	 * @param node	the json node of the website
	 * @return		a list of all TaskResult objects of the analysis
	 */
	public static List<TaskResult> analyseWebsite(JsonNode node) {
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
		
		// list to hold the Future objects associated with tasks
        List<Task> tasks = new ArrayList<Task>();
        
        // create task instances and add it to tasks list
        tasks.add(new DomainAgeTask(url));
        tasks.add(new DotsTask(url));
        tasks.add(new EmptyLinksTask(htmlDoc));
        tasks.add(new FormTagsTask(htmlDoc, url, listsHandler.getFormlist()));
        tasks.add(new FrameTask(htmlDoc, url));
        tasks.add(new InputTagsTask(htmlDoc));
        tasks.add(new IPAddressTask(url));
        tasks.add(new MetaTagsTask(htmlDoc, listsHandler.getMetalist()));
        tasks.add(new NoLinksBodyTask(htmlDoc));
        tasks.add(new NonmatchingUrlTask(htmlDoc));
        tasks.add(new OnlyScriptTask(htmlDoc));
        tasks.add(new PlaintextContentTask(htmlDoc, listsHandler.getContentlist()));
        tasks.add(new PunctuationTask(url));
        tasks.add(new SuspiciousLinksTask(htmlDoc));
        tasks.add(new SuspiciousScriptContentTask(htmlDoc, listsHandler.getScriptlist()));
        tasks.add(new SuspiciousUrlTask(url));
        tasks.add(new TitleTask(htmlDoc, url));
        tasks.add(new WebsiteIdentityTask(htmlDoc, url));
        
        // list of all TaskResult objects
        List<TaskResult> taskResults = new ArrayList<TaskResult>();
        
        try {
        	// invoke all tasks
	        List<Future<TaskResult>> resultFutures = executor.invokeAll(tasks);
	        
	        // collect results
	        for(Future<TaskResult> future : resultFutures){
	            try {
	                taskResults.add(future.get());
	            } catch (Exception e) {
	                LOGGER.warn(e.getClass().getSimpleName() + " while trying to get future result of a task");
	                return null;
	            }
	        }
	        
	        return taskResults;
	        
        } catch (Exception e) {
        	LOGGER.warn(e.getClass().getSimpleName() + " while invoking all tasks");
            return null;
        }
	}
}
