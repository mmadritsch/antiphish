package com.da.antiphish.corpus;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.tasks.DomainAgeTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * DomainAgeTaskThresholdCalculator calculates the best domain age threshold between 0 and 12 for the task 
 * DomainAgeTask based on the specified corpus analysis JSON file.
 * 
 * @author Marco Madritsch
 */
public class DomainAgeTaskThresholdCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainAgeTaskThresholdCalculator.class);
	private static final String corpusAnalysisPath = "C:/Users/Marco/Documents/Corpus/2017-11-29/training/training-analysis_2017-11-29.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29";
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the best domain age threshold 
	 * (threshold at which confidence of task is highest) for the task DomainAgeTask and saves result in an own JSON 
	 * file (specified path in variable resultPath).
	 */
	public static void main(String[] args) {
		
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	int domainAgeThreshold = calculateDomainAgeThreshold(corpusAnalysisList);
    	LOGGER.debug("Domain age threshold: " + domainAgeThreshold);
        
    	// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		
		// add threshold to json object
		node.put("domainAgeThreshold", domainAgeThreshold);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/domainAgeTaskTreshold.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving domain age task threshold to json file");
		}
	}
	
	/**
	 * Calculates the best domain age threshold (threshold at which confidence of task is highest) between 0 and 12
	 * for the task DomainAgeTask based on the given list of CorpusAnalysisEntry objects and returns it as int value.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @return						the best domain age threshold (between 0 and 12) as int value
	 */
	public static int calculateDomainAgeThreshold(List<CorpusAnalysisEntry> corpusAnalysisList) {
		int correct;
		int incorrect;
		int total;
		
		// hash map for storing confidence (success rate) for each domain age threshold
		Map<Integer, Double> confidences = new HashMap<Integer, Double>();
		
		for(int current=0; current <= 12; current++) {
			LOGGER.debug("Checking domain age threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
				// check if domain age (stored in addInfo) is not -1.0
	        	if(entry.getTaskResults().get(DomainAgeTask.class.getSimpleName()).get("addInfo") != -1.0) {
	        		// increase total count
	        		total++;
	        		
	        		// check if task would be correct with current domain age threshold
	        		if((!entry.getPhishing() && (entry.getTaskResults().get(DomainAgeTask.class.getSimpleName()).get("addInfo").intValue() > current))
	        			|| (entry.getPhishing() && (entry.getTaskResults().get(DomainAgeTask.class.getSimpleName()).get("addInfo").intValue() <= current))) {
	        			// increase correct count
	        			correct++;
	        		} else {
	        			// increase incorrect count
	        			incorrect++;
	        		}
//	        		LOGGER.debug("Phishing: " + entry.getPhishing() + ", months: " + entry.getTaskResults().get(DomainAgeTask.class.getSimpleName()).get("addInfo").intValue());
	        	}
	        	
			}
	        
			// check if total = correct + incorrect
			if(total != (correct + incorrect)) {
				LOGGER.warn("total != (correct + incorrect) for " + current);
			}
			
	        // add confidence for current domain age threshold to hashmap
	        double confidence = (double)correct/total;
	        confidences.put(current, confidence);
	        LOGGER.debug("Current domain age threshold: " + current + ", confidence (success rate): " + confidence);
	        
		}
		
		// return domain age threshold with highest confidence (success rate)
		// if multiple entries have the same confidence, min. threshold will be taken		
		Entry<Integer, Double> entryWithMaxConfidence = Collections.max(confidences.entrySet(), 
				new Comparator<Entry<Integer,Double>>(){
	            @Override
	            public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
	            	if(o1.getValue() > o2.getValue()) {
	            		return 1;
	            	} else if((o1.getValue().equals(o2.getValue())) && (o1.getKey() < o2.getKey())) {
	            		return 1;
	            	} else {
	            		return -1;
	            	}
	            }});
	
		LOGGER.debug("Return domain age threshold " + entryWithMaxConfidence.getKey() + ", confidence (success rate): " + entryWithMaxConfidence.getValue());
		return (int)entryWithMaxConfidence.getKey();
	}
}
