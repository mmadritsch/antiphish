package com.da.antiphish.analyse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.configuration.TaskConfiguration;

/**
 * WeightsCalculator is responsible for calculating all task weights which are necessary
 * for calculation of the final score of an analysis.
 * There are two different methods available for that:
 * <ul>
 * <li>normaliseTaskConfidences(): calculates all task weights based on task confidences</li>
 * <li>normaliseTaskEffects(): calculates all task weights based on task effects</li>
 * </ul>
 * 
 * @author Marco Madritsch
 */
public class WeightsCalculator {
	private final static Logger LOGGER = LoggerFactory.getLogger(WeightsCalculator.class);
	
	/**
	* Calculates all task weights based on task confidences and returns
	* a Map, where the task name represents the key and the normalised confidence the value.
	* @param taskConfigurationObjects	all TaskConfiguration objects
	* @return							Map<String, Double>, where the String value represents the task name 
	* 									and the Double value the normalised confidence if success,
	* 									null otherwise
	*/
	public Map<String, Double> normaliseTaskConfidences(Map<String, TaskConfiguration> taskConfigurationObjects) {
		Map<String, Double> normalisedWeights = new HashMap<String, Double>();
		
		try {
			LOGGER.debug("Calculating weights based on task confidences");
			
			// calculate sum of confidences
			double sumOfConfidences = 0.0;
			
			// iterate over map values (TaskConfiguration objects)
			for(TaskConfiguration taskConfiguration : taskConfigurationObjects.values()) {
				sumOfConfidences += taskConfiguration.getConfidence();
			}
			
			LOGGER.debug("Sum of confidences: " + sumOfConfidences);
            
			if(sumOfConfidences <= 0.0) {
				return null;
			}
			
            // calculate normalised weights based on sum of all confidences
			LOGGER.debug("Calculating normalised confidences:");
            for(Entry<String, TaskConfiguration> taskConfiguration : taskConfigurationObjects.entrySet()) {
            	LOGGER.debug("     " + taskConfiguration.getKey() + ": \t" + taskConfiguration.getValue().getConfidence() + "/"
    					+ sumOfConfidences + " = " + taskConfiguration.getValue().getConfidence()/sumOfConfidences);
            	
            	normalisedWeights.put(taskConfiguration.getKey(), taskConfiguration.getValue().getConfidence()/sumOfConfidences);
    		}
	        
            return normalisedWeights;
		} catch (Exception e) {
			LOGGER.error(e.getClass().getSimpleName() + " while calculating weights based on task confidences");
			return null;
        }
	}
	
	/**
	* Calculates all task weights based on task effects and returns
	* a Map, where the task name represents the key and the normalised confidence the value.
	* The effect of a task is it's true positive rate minus it's false positive rate.
	* @param taskConfigurationObjects	all TaskConfiguration objects
	* @return							Map<String, Double>, where the String value represents the task name 
	* 									and the Double value the normalised effect if success,
	* 									null otherwise
	*/
	public Map<String, Double> normaliseTaskEffects(Map<String, TaskConfiguration> taskConfigurationObjects) {
		Map<String, Double> normalisedWeights = new HashMap<String, Double>();
		
		try {
			LOGGER.debug("Calculating weights based on task effects");
			
			// calculate effects and sum of effects
			double sumOfEffects = 0.0;
			double effect = 0.0;
			
			// iterate over map values (TaskConfiguration objects)
			for(Entry<String, TaskConfiguration> taskConfiguration : taskConfigurationObjects.entrySet()) {
				// effect = true positive - false positive
				// effect = true positive - (1.0 - true negative)
				effect = taskConfiguration.getValue().getTruePositive() - (1.0 - taskConfiguration.getValue().getTrueNegative());
				
				// effect can't be negative
				if(effect < 0.0) {
					effect = 0.0;
				}
				
				// add it to hashmap
				normalisedWeights.put(taskConfiguration.getKey(), effect);
				sumOfEffects += effect;
			}
			
			LOGGER.debug("Sum of effects: " + sumOfEffects);
            
			if(sumOfEffects <= 0.0) {
				return null;
			}
			
            // calculate normalised weights based on sum of all effects
			LOGGER.debug("Calculating normalised effects:");
            for(Entry<String, Double> entry : normalisedWeights.entrySet()) {
            	LOGGER.debug("     " + entry.getKey() + ": \t" + entry.getValue() + "/"
    					+ sumOfEffects + " = " + entry.getValue()/sumOfEffects);
            	
            	normalisedWeights.put(entry.getKey(), entry.getValue()/sumOfEffects);
    		}
	        
            return normalisedWeights;
		} catch (Exception e) {
			LOGGER.error(e.getClass().getSimpleName() + " while calculating weights based on task effects");
			return null;
        }
	}
}
