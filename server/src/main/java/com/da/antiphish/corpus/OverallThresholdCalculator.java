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

import com.da.antiphish.analyse.ScoreSafetyLimitSelector;
import com.da.antiphish.analyse.WeightsCalculator;
import com.da.antiphish.configuration.ConfigurationHandler;
import com.da.antiphish.tasks.TaskResult;
import com.da.antiphish.types.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * OverallThresholdCalculator calculates the overall threshold of the whole system based on the specified corpus 
 * analysis JSON file. Stores the result in an own JSON file. This result has to be manually entered into config.json 
 * file and indicates up to which final score a website is classified as legitimate and above which as phishing site.
 * 
 * @author Marco Madritsch
 */
public class OverallThresholdCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverallThresholdCalculator.class);
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_2017-12-10.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();
	private static final WeightsCalculator weightsCalculator = new WeightsCalculator();
	private static final Map<String, Double> normalisedTaskWeights = 
			weightsCalculator.normaliseTaskEffects(configurationHandler.getConfiguration().getTasks());
//	private static final Map<String, Double> normalisedTaskWeights = 
//			weightsCalculator.normaliseTaskConfidences(configurationHandler.getConfiguration().getTasks());
	private static final ScoreSafetyLimitSelector scoreSafetyLimitSelector = 
			new ScoreSafetyLimitSelector(configurationHandler.getConfiguration());
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the best overall threshold of
	 * the whole system (threshold at which confidence of the system is highest) and saves result in an own JSON 
	 * file (specified path in variable resultPath).
	 */
	public static void main(String[] args) {
		// set log level to info --> less output, faster computation
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	double overallThreshold = calculateOverallThreshold(corpusAnalysisList);
    	LOGGER.info("Overall threshold: " + overallThreshold);
        
    	// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		
		// add overall threshold to json object
		node.put("overallThreshold", overallThreshold);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/overallThreshold.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving overall threshold to json file");
		}
	}
	
	/**
	 * Calculates the best overall threshold (threshold at which confidence of the whole system is highest) between 
	 * 0.0 and 1.0 based on the given list of CorpusAnalysisEntry objects and returns it as double value.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @return						the best overall threshold (between 0.0 and 1.0) as double value
	 */
	public static double calculateOverallThreshold(List<CorpusAnalysisEntry> corpusAnalysisList) {
		int correct;
		int incorrect;
		int total;
		
		// hash map for storing overall confidence (success rate) for each hit threshold
		Map<Double, Double> confidences = new HashMap<Double, Double>();
		
		for(double current=0.0; current < 1.0; current+=0.001) {
			LOGGER.debug("Checking overall threshold: " + current);
			correct = 0;
			incorrect = 0;
			total = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
				// convert task result to list of TaskResult objects for calculating final score
				List<TaskResult> taskResultList = new ArrayList<TaskResult>();
				for(Entry<String, Map<String, Double>> result : entry.getTaskResults().entrySet()) {
					// add task result as TaskResult object to list (category does not matter)
					taskResultList.add(new TaskResult(result.getKey(), Category.UNDEFINED, result.getValue()
							.get("score")));
				}
				
				// calculate final score
				double finalScore = calculateFinalScore(taskResultList);
				
        		// increase total count
        		total++;
        		
        		// check if labeling was correct or not according to current threshold
        		if((!entry.getPhishing() && (finalScore <= current)) ||
        			(entry.getPhishing() && (finalScore > current))) {
        			// increase correct count
        			correct++;
        		} else {
        			// increase incorrect count
        			incorrect++;
        		}
	        	
			}
	        
			// check if total = correct + incorrect
			if(total != (correct + incorrect)) {
				LOGGER.warn("total != (correct + incorrect) for " + current);
			}
			
	        // add confidence for current threshold to hashmap
	        double confidence = (double)correct/total;
	        confidences.put(current, confidence);
	        LOGGER.debug("Current overall threshold: " + current + ", confidence (success rate): " + confidence);
	        
		}
		
		// return overall threshold with highest confidence (success rate)
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
	
		LOGGER.info("Return overall threshold " + entryWithMaxConfidence.getKey() + ", confidence (success rate): " 
				+ entryWithMaxConfidence.getValue());
		return (double)entryWithMaxConfidence.getKey();
	}
	
	/**
	* Calculates the final score of the analysis. The final score is the sum of all weighted scores of all TaskResult 
	* objects.
	* @param taskResults	the TaskResult objects of the analysis
	* @return				the final score of the analysis as double between 0.0 and 1.0
	*/
	private static double calculateFinalScore(List<TaskResult> taskResults) {
		double finalScore = 0.0;
		
		// iterate over result objects and sum up scores (exclude failed tasks - where score is equal to -1.0)
		LOGGER.debug("Calculating final score...");
		for(TaskResult result : taskResults) {
			// check if task did not fail and normalised task confidence is available
			if(result.getScore() != -1.0 && normalisedTaskWeights.containsKey(result.getTaskName())) {
				// add weighted score to final score
				finalScore += result.getScore() * normalisedTaskWeights.get(result.getTaskName());
				
				LOGGER.debug("   Adding " + result.getTaskName() + ": " + result.getScore() + " * " 
						+ normalisedTaskWeights.get(result.getTaskName())
						+ " = " + result.getScore() * normalisedTaskWeights.get(result.getTaskName()));
			} else {
				LOGGER.debug("   Skipping " + result.getTaskName() + ": " + result.getScore());
			}
		}
		
		// check thresholds for each task
		double thresholdCheckResult = scoreSafetyLimitSelector.takeMajority(taskResults);
		if(thresholdCheckResult != -1.0) {
			LOGGER.debug("Returning a final score of " + thresholdCheckResult + " based on threshold score selector");
			
			return thresholdCheckResult;
		}
		
		if(finalScore > 1.0) {
			finalScore = 1.0;
		}
		
		LOGGER.debug("Returning a calculated final score of " + finalScore);
		
		return finalScore;
	}
}
