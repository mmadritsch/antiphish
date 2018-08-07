package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
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
 * AccuracyCalculator calculates the accuracy of the whole system based on a corpus analysis JSON file and saves 
 * results (accuracy, true positive, false negative, true negative and false positive rate) in an own JSON file.
 * 
 * @author Marco Madritsch
 */
public class AccuracyCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccuracyCalculator.class);
//	private static final String corpusAnalysisPath = 
//			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_new.json";
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/test/test-analysis_2017-12-10.json"; // path to corpus file
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";	// path for storing results
	private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();
	private static final WeightsCalculator weightsCalculator = new WeightsCalculator();
	private static Map<String, Double> normalisedTaskWeights = 
			weightsCalculator.normaliseTaskEffects(configurationHandler.getConfiguration().getTasks());
//	private static Map<String, Double> normalisedTaskWeights = 
//			weightsCalculator.normaliseTaskConfidences(configurationHandler.getConfiguration().getTasks());
	private static final ScoreSafetyLimitSelector scoreSafetyLimitSelector = 
			new ScoreSafetyLimitSelector(configurationHandler.getConfiguration());
	
	/**
	 * Reads all corpus analysis results out of the json file based on the specified path in variable 
	 * corpusAnalysisPath, calls the method to calculate the accuracy and stores the result into a json file based on 
	 * the specified path in variable resultPath.
	 */
	public static void main(String[] args) {
		// set log level to info --> less output, faster computation
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
				
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	double[] results = calculateAccuracy(corpusAnalysisList);
    	LOGGER.info("Overall accuracy: " + results[0]);
        
    	// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode node = mapper.createObjectNode();
		
		// add results to json object
		node.put("accuracy", results[0]);
		node.put("truePositive", results[1]);
		node.put("falseNegative", results[2]);
		node.put("trueNegative", results[3]);
		node.put("falsePositive", results[4]);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/accuracy.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving accuracy to json file");
		}
	}
	
	/**
	 * Calculates the accuracy values (accuracy, true positive rate, false negative rate, true negative rate and false
	 * positive rate) of the whole system based on the given list of CorpusAnalysisEntry objects.
	 * @param corpusAnalysisList	list of CorpusAnalysisEntry objects
	 * @return						accuracy values in an integer array
	 */
	public static double[] calculateAccuracy(List<CorpusAnalysisEntry> corpusAnalysisList) {
		int correct = 0;
		int incorrect = 0;
		int total = 0;
		int falsePositive = 0;
		int falseNegative = 0;
		int truePositive = 0;
		int trueNegative = 0;
		
		double falsePositiveAverageScore = 0.0;
		double falseNegativeAverageScore = 0.0;
		Map<String, Integer> taskFalsePositiveStatistics = new HashMap<String, Integer>();
		Map<String, Integer> taskFalseNegativeStatistics = new HashMap<String, Integer>();
		LOGGER.debug("Checking accuracy...");
		
		// iterate over each corpus analysis result (= all analysed websites)
		for(CorpusAnalysisEntry entry : corpusAnalysisList) {
			// convert task result to list of TaskResult objects for calculating final score
			List<TaskResult> taskResultList = new ArrayList<TaskResult>();
			for(Entry<String, Map<String, Double>> result : entry.getTaskResults().entrySet()) {
				// add task result as TaskResult object to list (category does not matter)
				if(configurationHandler.getConfiguration().getTasks().containsKey(result.getKey())) {
					taskResultList.add(new TaskResult(result.getKey(), Category.UNDEFINED, 
							result.getValue().get("score")));
				}
			}
			
			// calculate final score
			double finalScore = calculateFinalScore(taskResultList);
			
    		// increase total count
    		total++;
    		
    		// check if labeling was correct or not according to overall threshold
    		if(!entry.getPhishing() && (finalScore <= configurationHandler.getConfiguration().getOverallThreshold())) {
    			// increase correct count
    			correct++;
    			trueNegative++;
    		} else if((entry.getPhishing() 
    				&& (finalScore > configurationHandler.getConfiguration().getOverallThreshold()))) {
    			// increase correct count
    			correct++;
    			truePositive++;
    		} else {
    			// increase incorrect count
    			incorrect++;
    			
    			if(!entry.getPhishing() 
    					&& (finalScore > configurationHandler.getConfiguration().getOverallThreshold())) {
    				falsePositive++;
    				falsePositiveAverageScore += finalScore;
    				
    				for(TaskResult taskResult : taskResultList) {
        				if(taskResult.getScore() > 0.0) {
    	    				if(taskFalsePositiveStatistics.containsKey(taskResult.getTaskName())) {
    	    					int count = taskFalsePositiveStatistics.get(taskResult.getTaskName());
    	    					count++;
    	    					taskFalsePositiveStatistics.put(taskResult.getTaskName(), count);
    	    				} else {
    	    					taskFalsePositiveStatistics.put(taskResult.getTaskName(), 1);
    	    				}
        				}
        			}
    			} else {
    				falseNegative++;
    				falseNegativeAverageScore += finalScore;
    				
    				for(TaskResult taskResult : taskResultList) {
        				if(taskResult.getScore() == 0.0) {
    	    				if(taskFalseNegativeStatistics.containsKey(taskResult.getTaskName())) {
    	    					int count = taskFalseNegativeStatistics.get(taskResult.getTaskName());
    	    					count++;
    	    					taskFalseNegativeStatistics.put(taskResult.getTaskName(), count);
    	    				} else {
    	    					taskFalseNegativeStatistics.put(taskResult.getTaskName(), 1);
    	    				}
        				}
        			}
    			}
    		}
        	
		}
        
		// check if total = correct + incorrect
		if(total != (correct + incorrect)) {
			LOGGER.warn("total != (correct + incorrect) ");
		}
		
        // calculate accuracy
        double accuracy = (double)correct/total;
        double falsePositiveRate = (double)falsePositive/(corpusAnalysisList.size()/2);
        double falseNegativeRate = (double)falseNegative/(corpusAnalysisList.size()/2);
        double truePositiveRate = (double)truePositive/(corpusAnalysisList.size()/2);
        double trueNegativeRate = (double)trueNegative/(corpusAnalysisList.size()/2);
	
		LOGGER.info("Return overall accuracy of " + accuracy);		
		LOGGER.info("True positive: " + truePositiveRate);
		LOGGER.info("False negative: " + falseNegativeRate);
		LOGGER.info("True negative: " + trueNegativeRate);
		LOGGER.info("False positive: " + falsePositiveRate);
		LOGGER.info("False positive statistics: " + taskFalsePositiveStatistics.toString());
		LOGGER.info("False negative statistics: " + taskFalseNegativeStatistics.toString());
		LOGGER.info("Average false positive score: " + (falsePositiveAverageScore/falsePositive));
		LOGGER.info("Average false negative score: " + (falseNegativeAverageScore/falseNegative));
		
		return new double[]{accuracy, truePositiveRate, falseNegativeRate, trueNegativeRate, falsePositiveRate};
	}
	
	/**
	* Calculates the final score of the analysis.
	* The final score is the sum of all weighted scores of the TaskResult objects.
	* @param taskResults	the TaskResult objects
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
		
		// check safety limits for each task
		double safetyLimitCheckResult = scoreSafetyLimitSelector.takeMajority(taskResults);
		if(safetyLimitCheckResult != -1.0) {
			LOGGER.debug("Returning a final score of " + safetyLimitCheckResult + " based on score safety limit selector");
			
			return safetyLimitCheckResult;
		}
		
		if(finalScore > 1.0) {
			finalScore = 1.0;
		}
		
		LOGGER.debug("Returning a calculated final score of " + finalScore);
		
		return finalScore;
	}
}
