package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.da.antiphish.tasks.TitleTask;
import com.da.antiphish.tasks.WebsiteIdentityTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * TaskConfidenceCalculator calculates the confidence and threshold of each task based on the specified corpus analysis 
 * JSON file and stores the result in an own JSON file. This result has to be manually entered into config.json file.
 * 
 * @author Marco Madritsch
 */
public class TaskConfidenceCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskConfidenceCalculator.class);
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_new.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the best thresholds
	 * (threshold at which confidence of task is highest) for all tasks and saves the results in an own JSON file 
	 * (specified path in variable resultPath).
	 */
	public static void main(String[] args) {

		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = 
				CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        		
        // list of all task names
 		List<String> taskNames = new ArrayList<String>();
 		taskNames.add(DomainAgeTask.class.getSimpleName());
 		taskNames.add(DotsTask.class.getSimpleName());
 		taskNames.add(EmptyLinksTask.class.getSimpleName());
 		taskNames.add(FormTagsTask.class.getSimpleName());
 		taskNames.add(FrameTask.class.getSimpleName());
 		taskNames.add(InputTagsTask.class.getSimpleName());
 		taskNames.add(IPAddressTask.class.getSimpleName());
 		taskNames.add(MetaTagsTask.class.getSimpleName());
 		taskNames.add(NoLinksBodyTask.class.getSimpleName());
 		taskNames.add(NonmatchingUrlTask.class.getSimpleName());
 		taskNames.add(OnlyScriptTask.class.getSimpleName());
 		taskNames.add(PunctuationTask.class.getSimpleName());
 		taskNames.add(PlaintextContentTask.class.getSimpleName());
 		taskNames.add(SuspiciousLinksTask.class.getSimpleName());
 		taskNames.add(SuspiciousScriptContentTask.class.getSimpleName());
 		taskNames.add(SuspiciousUrlTask.class.getSimpleName());
 		taskNames.add(TitleTask.class.getSimpleName());
 		taskNames.add(WebsiteIdentityTask.class.getSimpleName());
        
 		// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		ObjectNode innerNode;
        
		// calculate thresholds for each task
        for(String taskName : taskNames) {
    		innerNode = mapper.createObjectNode();
    		
    		// calculate confidence and threshold
        	double[] result = calculateConfidence(corpusAnalysisList, taskName);
        	LOGGER.debug(taskName + ": threshold " + result[0] + ", confidence " + result[1] + ", true postive: " 
        			+ result[2] + ", true negative: " + result[3]);
        	
        	// add task's confidence and threshold to json object
        	innerNode.put("threshold", result[0]);
        	innerNode.put("confidence", result[1]);
        	innerNode.put("truePositive", result[2]);
        	innerNode.put("trueNegative", result[3]);
        	node.set(taskName, innerNode);
        }
        
        // save confidences and thresholds as json file
        try {
			mapper.writeValue(new File(resultPath + "/taskConfidences.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving confidences to json file");
		}
	}
	
	/**
	 * Calculates the confidence and the associated threshold of a task based on the given list of CorpusAnalysisEntry
	 * objects.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @param taskName				the name of the task which's threshold and confidence should be calculated
	 * @return						a double array where at 
	 * 								index 0 the (best) threshold 
	 * 								index 1 the associated confidence
	 * 								index 2 the associated true positive rate
	 * 								index 3 the associated true negative rate is stored
	 */
	public static double[] calculateConfidence(List<CorpusAnalysisEntry> corpusAnalysisList, String taskName) {
		int correct;
		int incorrect;
		int total;
		int truePositive;
		int trueNegative;
		int numberOfPhishs;
		int numberOfLegitimates;
		
		// hash map for storing confidence (success rate), true positive and true negative rate for each threshold
		Map<Double, Double[]> confidences = new HashMap<Double, Double[]>();
		
		for(double current=0.0; current < 1.0; current+=0.01) {
			LOGGER.debug("Checking threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			truePositive = 0;
			trueNegative = 0;
			numberOfPhishs = 0;
			numberOfLegitimates = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
	        	// check if task was successful, otherwise skip it
	        	if(entry.getTaskResults().get(taskName).get("score") != -1.0) {
	        		// increase total count
	        		total++;
	        		
	        		if(entry.getPhishing()) {
	        			numberOfPhishs++;
	        		} else {
	        			numberOfLegitimates++;
	        		}
	        		
	        		// check if task was correct or not according to current threshold
	        		if(!entry.getPhishing() && (entry.getTaskResults().get(taskName).get("score") <= current)) {
	        			correct++;
	        			trueNegative++;
	        		} else if(entry.getPhishing() && (entry.getTaskResults().get(taskName).get("score") > current)) {
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
	        
	        // add values for current threshold to hashmap
	        double confidence = (double)correct/total;
	        double truePositiveRate = (double)truePositive/numberOfPhishs;
	        double trueNegativeRate = (double)trueNegative/numberOfLegitimates;
	        confidences.put(current, new Double[]{confidence, truePositiveRate, trueNegativeRate});
	        LOGGER.debug("Current threshold: " + current + ", confidence (success rate): " + confidence 
	        		+ ", true positive: " + truePositiveRate + ", true negative: " + trueNegativeRate);
		}
		
		// return threshold with highest confidence (success rate)
		// if multiple entries have the same confidence, min. threshold will be taken		
		Entry<Double, Double[]> entryWithMaxConfidence = Collections.max(confidences.entrySet(), 
				new Comparator<Entry<Double,Double[]>>(){
	            @Override
	            public int compare(Entry<Double, Double[]> o1, Entry<Double, Double[]> o2) {
	            	if(o1.getValue()[0] > o2.getValue()[0]) {
	            		return 1;
	            	} else if((o1.getValue()[0].equals(o2.getValue()[0])) && (o1.getKey() < o2.getKey())) {
	            		return 1;
	            	} else {
	            		return -1;
	            	}
	            }});
	
		LOGGER.debug("Return threshold " + entryWithMaxConfidence.getKey());
		return new double[]{(double)entryWithMaxConfidence.getKey(), entryWithMaxConfidence.getValue()[0],
				entryWithMaxConfidence.getValue()[1], entryWithMaxConfidence.getValue()[2]};
		
	}
}
