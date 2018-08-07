package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
 * RangesCalculator calculates the ranges for the traffic light based on the specified corpus analysis JSON file and 
 * stores the result in an own JSON file. This result has to be manually entered into config.json file.
 * 
 * @author Marco Madritsch
 */
public class RangesCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(RangesCalculator.class);
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_2017-12-10.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();
	private static final WeightsCalculator weightsCalculator = new WeightsCalculator();
	private static final Map<String, Double> normalisedTaskWeights = 
			weightsCalculator.normaliseTaskEffects(configurationHandler.getConfiguration().getTasks());
	private static final ScoreSafetyLimitSelector scoreSafetyLimitSelector = 
			new ScoreSafetyLimitSelector(configurationHandler.getConfiguration());
	
	/**
	 * Reads all corpus analysis results out of the json file based on the specified path in variable 
	 * corpusAnalysisPath, calls the method to calculate the ranges for the traffic lights and stores the result into 
	 * a json file under the specified path in variable resultPath.
	 */
	public static void main(String[] args) {
		// set log level to info --> less output, faster computation
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// read corpus analysis results from file
		List<CorpusAnalysisEntry> corpusAnalysisList = CorpusHelper.readCorpusAnalysisFromFile(new File(corpusAnalysisPath));
        
    	double[] splitValues = calculateRangeSplitValues(corpusAnalysisList);
    	
    	// check if split values exist
    	if(splitValues != null) {
	    	LOGGER.info("Green to yellow split value: " + splitValues[0] + ", yellow to red split value: " 
	    			+ splitValues[1]);
	        
	    	// mapper and node to store result as json file
	 		ObjectMapper mapper = new ObjectMapper();
	 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
			ObjectNode node = mapper.createObjectNode();
			
			// add range split values to json object
			node.put("greenYellowSplitValue", splitValues[0]);
			node.put("yellowRedSplitValue", splitValues[1]);
	    	
			// save results as json file
	        try {
				mapper.writeValue(new File(resultPath + "/rangeSplitValues.json"), node);
			} catch (Exception e) {
				LOGGER.warn(e.getClass().getSimpleName() + " while saving overall hit thresholds to json file");
			}
    	} else {
    		LOGGER.warn("Range split values could not be calculated");
    	}
	}
	
	/**
	 * Calculates the range split values for the traffic lights (green, yellow, red) based on the given list of 
	 * CorpusAnalysisEntry objects. Iterates therefore over all list elements (= all analysed websites), calculates
	 * the final scores and stores them in an own list (overallScores). Sorts this list at the end and determines the
	 * range split values by splitting the range of all final scores in 3 parts.
	 * @param corpusAnalysisList	list of CorpusAnalysisEntry objects
	 * @return						range split values in a double array
	 */
	public static double[] calculateRangeSplitValues(List<CorpusAnalysisEntry> corpusAnalysisList) {
		
		// list to store all overall score results
		List<Double> overallScores = new ArrayList<Double>();
		
		// iterate over each corpus analysis result (= all analysed websites)
		for(CorpusAnalysisEntry entry : corpusAnalysisList) {
			// convert task result to list of TaskResult objects for calculating final score
			List<TaskResult> taskResultList = new ArrayList<TaskResult>();
			for(Entry<String, Map<String, Double>> result : entry.getTaskResults().entrySet()) {
				// add task result as TaskResult object to list (category does not matter)
				taskResultList.add(new TaskResult(result.getKey(), Category.UNDEFINED, result.getValue().get("score")));
			}
			
			// calculate final score
			double finalScore = calculateFinalScore(taskResultList);
			
			overallScores.add(finalScore);
			
//    		// add score to list if it does not appear already
//			if(!overallScores.contains(finalScore)) {
//				overallScores.add(finalScore);
//			}
		}
		
		// sort list
		Collections.sort(overallScores);
		
		if(overallScores.size() >= 3) {
			// get range split values
			int interval = overallScores.size()/3;
			int firstSplitIndex = interval;
			int secondSplitIndex = 2*interval;
			
			while((overallScores.get(firstSplitIndex) == overallScores.get(firstSplitIndex-1))
					&& (firstSplitIndex < secondSplitIndex)) {
				firstSplitIndex++;
			}
			
			while((overallScores.get(secondSplitIndex) == overallScores.get(secondSplitIndex-1))
					&& (secondSplitIndex < overallScores.size())) {
				secondSplitIndex++;
			}
			
			return (new double[]{overallScores.get(firstSplitIndex), overallScores.get(secondSplitIndex)});
		} else {
			return null;
		}
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
//		LOGGER.debug("Calculating final score...");
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
