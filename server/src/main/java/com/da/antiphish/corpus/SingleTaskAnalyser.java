package com.da.antiphish.corpus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import com.fasterxml.jackson.databind.JsonNode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * SingleTaskAnalyser calculates the confidence and threshold of a single task based on the specified (training) corpus. 
 * It is meant to get a quick result if some changes were made to a single task.
 * 
 * @author Marco Madritsch
 */
public class SingleTaskAnalyser {
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleTaskAnalyser.class);
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final ListsHandler listsHandler = new ListsHandler();
	
	/**
	 * Analyses all websites from a (training) corpus (path in variable trainingPath) with a single task in order to 
	 * calculate the confidence and threshold for that task.
	 */
	public static void main(String[] args) {
		// set log level to info --> less output, faster computation
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
        // analyse training corpus
        File trainingPhishingDirectory = new File(trainingPath + "/phishing");
		File trainingLegitimateDirectory = new File(trainingPath + "/legitimate");
		File[] trainingFiles = ArrayUtils.addAll(trainingPhishingDirectory.listFiles(), 
				trainingLegitimateDirectory.listFiles());
		
		// task name has to be specified as second parameter
		List<Pair<Double, Boolean>> taskResults = analyseFiles(trainingFiles, FormTagsTask.class.getSimpleName());
        
    	double[] result = calculateTaskConfidence(taskResults);
    	LOGGER.info("Task Threshold: " + result[0]);
    	LOGGER.info("Task Confidence: " + result[1]);
	}
	
	/**
	 * Reads each website from the given array of files, analyses it with the specified task and stores the results in 
	 * a json file.
	 * @param files			the array of all files (websites) to be analysed
	 * @param taskName		the name of the task that should be used for the analysis
	 * @return 				a list of Pairs (Double, Boolean) where the Double represents the score of a website
	 * 						and the Boolean the phishing indicator
	 */
	public static List<Pair<Double, Boolean>> analyseFiles(File[] files, String taskName) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		try {
			// list of all scores and phishing indicators of that task
	 		List<Pair<Double, Boolean>> taskResults = new ArrayList<Pair<Double, Boolean>>();
	 		
	 		// score
	 		double score;
	 		
	 		// node for each website which will be read from json
      		JsonNode websiteNode;
     		
	 		// read each website from file and analyse it
	        for(File file : files) {
	        	total++;
	        	
	        	// read website from file
	        	websiteNode = CorpusHelper.readWebsiteFromFile(file);
	            
	            // analyse website
	        	score = analyseWebsite(websiteNode, taskName);
	            
	            // check task score for failure (-2.0)
	            if(score != -2.0) {
	            	success++;
	            	
	            	// phishing indicator
	            	if(file.getName().contains("phishing")) {
	            		taskResults.add(Pair.of(score, true));
	            	} else {
	            		taskResults.add(Pair.of(score, false));
	            	}	            	
	            } else {
	            	fail++;
	            	LOGGER.warn("Failed to analyse website: " + fail);
	            }
	            
	            LOGGER.info("Analysed websites: " + total);
	        }
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successful, " + fail + " failed");
	        
	        return taskResults;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
	
	/**
	 * Analyses a single website with the given task.
	 * @param node		the json node of the website
	 * @param taskName	the name of the task that should be used for the analysis
	 * @return			the calculated score of the task
	 */
	public static double analyseWebsite(JsonNode node, String taskName) {
		URL url;
		try {
			url = new URL(node.path("url").asText());
		} catch (MalformedURLException e) {
			return -2.0;
		}
		
		// get html content
		Document htmlDoc = Jsoup.parse(node.path("content").asText());
		
		// check if content is null
		if(htmlDoc == null) {
			return -2.0;
		}
        
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
		        
	        	// return score
		        return taskResult.getScore();
		        
	        } catch (Exception e) {
	        	LOGGER.warn(e.getClass().getSimpleName() + " while invoking task");
	            return -2.0;
	        }
        } else {
        	return -2.0;
        }
	}
	
	/**
	 * Calculates the confidence and the associated threshold of a task based on the given list of Pairs 
	 * (Double, Boolean) where the Double represents the calculated score of a website and the Boolean the phishing 
	 * indicator.
	 * @param taskResults	the list of Pairs (Double, Boolean) of all analysed websites
	 * @return				a double array where at index 0 the (best) threshold is stored and at index 1 the
	 * 						associated confidence
	 */
	public static double[] calculateTaskConfidence(List<Pair<Double, Boolean>> taskResults) {
		int correct;
		int incorrect;
		int total;
		int truePositive;
		int trueNegative;
		
		// hash map for storing confidence (success rate) for each threshold
		Map<Double, Double> confidences = new HashMap<Double, Double>();
		
		for(double current=0.0; current < 1.0; current+=0.01) {
			LOGGER.debug("Checking threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			truePositive = 0;
			trueNegative = 0;
			
			// iterate over each task result and analyse it
			for(Pair<Double, Boolean> pair : taskResults) {
	        	// check if task was successful, otherwise skip it
	        	if(pair.getLeft() != -1.0) {
	        		// increase total count
	        		total++;
	        		
	        		// check if task was correct or not according to current threshold
	        		if(!pair.getRight() && (pair.getLeft() <= current)) {
	        			correct++;
	        			trueNegative++;
	        		} else if(pair.getRight() && (pair.getLeft() > current)) {	// check if task was right
	        			correct++;
	        			truePositive++;
	        		} else {
	        			// increase incorrect count
	        			incorrect++;
	        		}
	        	}
			}
			
			// check if total = correct + incorrect
			if(total != (correct + incorrect)) {
				LOGGER.warn("total != (correct + incorrect) for " + current);
			}
			
	        // add confidence for current threshold to hashmap
	        double confidence = (double)correct/total;
	        double truePositiveRate = (double)truePositive/(taskResults.size()/2);
	        double trueNegativeRate = (double)trueNegative/(taskResults.size()/2);
	        confidences.put(current, confidence);
	        LOGGER.info("Current threshold: " + current + ", confidence (success rate): " + confidence 
	        		+ ", correct: " + correct + ", total: " + total);
	        LOGGER.info("True positive: " + truePositiveRate + ", True neagtive: " + trueNegativeRate);
		}
		
		// return threshold with highest confidence (success rate)
		// if multiple entries have the same confidence, min. threshold will be taken		
		Entry<Double, Double> entryWithMaxConfidence = Collections.max(confidences.entrySet(), 
				new Comparator<Entry<Double,Double>>(){
	            @Override
	            public int compare(Entry<Double, Double> o1, Entry<Double, Double> o2) {
	            	if(o1.getValue() > o2.getValue()) {
	            		return 1;
	            	} else if((o1.getValue().equals(o2.getValue())) && (o1.getKey() < o2.getKey())) {
	            		return 1;
	            	} else {
	            		return -1;
	            	}
	            }});
	
		LOGGER.debug("Return threshold " + entryWithMaxConfidence.getKey() + ", confidence (success rate): " 
				+ entryWithMaxConfidence.getValue());
		return new double[]{(double)entryWithMaxConfidence.getKey(), (double)entryWithMaxConfidence.getValue()};
	}
}
