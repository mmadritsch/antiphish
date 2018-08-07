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

import com.da.antiphish.tasks.PunctuationTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * PunctuationTaskThresholdCalculator calculates the best punctuation threshold between 0 and 70 for task 
 * PunctuationTask based on the specified corpus analysis JSON file.
 * 
 * @author Marco Madritsch
 */
public class PunctuationTaskThresholdCalculator {
	private final static Logger LOGGER = LoggerFactory.getLogger(PunctuationTaskThresholdCalculator.class);
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_new.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the best punctuation threshold 
	 * (threshold at which confidence of task is highest) for the task PunctuationTask and saves result in an own JSON 
	 * file (specified path in variable resultPath).
	 */
	public static void main(String[] args) {
		
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	int punctuationThreshold = calculatePunctuationThreshold(corpusAnalysisList);
    	LOGGER.debug("Punctuation threshold: " + punctuationThreshold);
        
    	// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		
		// add threshold to json object
		node.put("punctuationThreshold", punctuationThreshold);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/punctuationTaskTreshold.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving punctuation task threshold to json file");
		}
	}
	
	/**
	 * Calculates the best punctuation threshold (threshold at which confidence of task is highest) between 0 and 70
	 * for the task PunctuationTask based on the given list of CorpusAnalysisEntry objects and returns it as int value.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @return						the best punctuation threshold (between 0 and 70) as int value
	 */
	public static int calculatePunctuationThreshold(List<CorpusAnalysisEntry> corpusAnalysisList) {
		int correct;
		int incorrect;
		int total;
		
		// hash map for storing confidence (success rate) for each punctuation threshold
		Map<Integer, Double> confidences = new HashMap<Integer, Double>();
		
		// count max. number of punctuations of all legitimate sites
		int maxPunctuationsLegitimate = 0;
				
		for(int current=0; current <= 70; current++) {
			LOGGER.debug("Checking punctuation threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
				// check if number of punctuations (stored in addInfo) is not -1.0
	        	if(entry.getTaskResults().get(PunctuationTask.class.getSimpleName()).get("addInfo") != -1.0) {
	        		// increase total count
	        		total++;
	        		
	        		// check if task would be correct with current punctuation threshold
	        		if((!entry.getPhishing() && (entry.getTaskResults().get(PunctuationTask.class.getSimpleName()).get("addInfo").intValue() <= current))
	        			|| (entry.getPhishing() && (entry.getTaskResults().get(PunctuationTask.class.getSimpleName()).get("addInfo").intValue() > current))) {
	        			// increase correct count
	        			correct++;
	        		} else {
	        			// increase incorrect count
	        			incorrect++;
	        		}
	        	}
	        	
	        	// max punctuations in legitimate sites
	        	if(!entry.getPhishing() && maxPunctuationsLegitimate < entry.getTaskResults().get(PunctuationTask.class.getSimpleName()).get("addInfo").intValue()) {
	        		maxPunctuationsLegitimate = entry.getTaskResults().get(PunctuationTask.class.getSimpleName()).get("addInfo").intValue();
	        	}
	        	
			}
			
			LOGGER.info("Max. legitimate punctuations: " + maxPunctuationsLegitimate);
			
			// check if total = correct + incorrect
			if(total != (correct + incorrect)) {
				LOGGER.warn("total != (correct + incorrect) for " + current);
			}
	        
	        // add confidence for current punctuation threshold to hashmap
	        double confidence = (double)correct/total;
	        confidences.put(current, confidence);
	        LOGGER.debug("Current punctuation threshold: " + current + ", confidence (success rate): " + confidence);
	        
		}
		
		// return punctuation threshold with highest confidence (success rate)
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
	
		LOGGER.debug("Return punctuation threshold " + entryWithMaxConfidence.getKey() + ", confidence (success rate): " + entryWithMaxConfidence.getValue());
		return (int)entryWithMaxConfidence.getKey();
	}
}
