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

import com.da.antiphish.tasks.EmptyLinksTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * EmptyLinksTaskThresholdCalculator calculates the best empty links rate threshold between 0.0 and 1.0 for task 
 * EmptyLinksTask based on the specified corpus analysis JSON file.
 * 
 * @author Marco Madritsch
 */
public class EmptyLinksTaskThresholdCalculator {
	private final static Logger LOGGER = LoggerFactory.getLogger(EmptyLinksTaskThresholdCalculator.class);
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_new.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the best empty links rate 
	 * threshold (threshold at which confidence of task is highest) for the task EmptyLinksTask and saves result in an 
	 * own JSON file (specified path in variable resultPath).
	 */
	public static void main(String[] args) {
		
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	double emptyLinksThreshold = calculateEmptyLinksThreshold(corpusAnalysisList);
    	LOGGER.debug("Empty links threshold: " + emptyLinksThreshold);
        
    	// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		
		// add threshold to json object
		node.put("emptyLinksThreshold", emptyLinksThreshold);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/emptyLinksTaskTreshold.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving empty links task threshold to json file");
		}
	}
	
	/**
	 * Calculates the best empty links rate threshold (threshold at which confidence of task is highest) between 
	 * 0.0 and 1.0 for the task EmptyLinksTask based on the given list of CorpusAnalysisEntry objects and returns it 
	 * as double value.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @return						the best empty links rate threshold (between 0.0 and 1.0) as double value
	 */
	public static double calculateEmptyLinksThreshold(List<CorpusAnalysisEntry> corpusAnalysisList) {
		int correct;
		int incorrect;
		int total;
		
		// hash map for storing confidence (success rate) for each parameter threshold
		Map<Double, Double> confidences = new HashMap<Double, Double>();
		
		for(double current=0.0; current <= 1.0; current+=0.01) {
			LOGGER.debug("Checking empty links threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
				// check if empty links rate (stored in addInfo) is not -1.0
	        	if(entry.getTaskResults().get(EmptyLinksTask.class.getSimpleName()).get("addInfo") != -1.0) {
	        		// increase total count
	        		total++;
	        		
	        		// check if task would be correct with current empty links threshold
	        		if((!entry.getPhishing() && (entry.getTaskResults().get(EmptyLinksTask.class.getSimpleName()).get("addInfo") <= current))
	        			|| (entry.getPhishing() && (entry.getTaskResults().get(EmptyLinksTask.class.getSimpleName()).get("addInfo") > current))) {
	        			// increase correct count
	        			correct++;
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
	        
	        // add confidence for current empty links threshold to hashmap
	        double confidence = (double)correct/total;
	        confidences.put(current, confidence);
	        LOGGER.debug("Current empty links threshold: " + current + ", confidence (success rate): " + confidence); 
		}
		
		// return empty links threshold with highest confidence (success rate)
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
	
		LOGGER.debug("Return empty links threshold " + entryWithMaxConfidence.getKey() + ", confidence (success rate): " + entryWithMaxConfidence.getValue());
		return (double)entryWithMaxConfidence.getKey();
	}
}
