package com.da.antiphish.analyse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.configuration.Configuration;
import com.da.antiphish.tasks.TaskResult;

/**
 * ScoreSafetyLimitSelector is responsible for checking all single task scores against their
 * safety limits according to values stored in configuration (config.json).
 * If multiple safety limits are overrun, altogether three different methods are available
 * for selecting overall result:
 * <ul>
 * <li>takeFirst(): takes the first exceedance of safety limit as result</li>
 * <li>takeHighestConfidence(): takes the exceedance of safety limit which's corresponding task has the highest confidence as result</li>
 * <li>takeMajority(): takes the majority of all exceedances of safety limits as result</li>
 * </ul>
 * 
 * @author Marco Madritsch
 */
public class ScoreSafetyLimitSelector {
	private final static Logger LOGGER = LoggerFactory.getLogger(ScoreSafetyLimitSelector.class);
	private Configuration configuration;
	
	public ScoreSafetyLimitSelector(Configuration configuration) {
		this.configuration = configuration;
	}
	
	/**
	* Checks for each task result if the score is above it's upper safety limit or
	* under it's lower safety limit and selects the first found exceedance as result.
	* @param taskResults	list of all TaskResult objects
	* @return				the safety limit result,
	* 						0.0 if first found exceedance is an exceedance of lower limit,
	* 						1.0 if first found exceedance is an exceedance of upper limit, 
	* 						-1.0 if no exceedance was found or an error occurred
	*/
	public double takeFirst(List<TaskResult> taskResults) {
		try {
			LOGGER.debug("Checking task safety limits with method takeFirst");
			// iterate over each TaskResult and compare the task's score with safety limits from configuration
			for(TaskResult result : taskResults) {
				// check if configuration contains an entry for that task and score is != -1.0
				if(configuration.getTasks().containsKey(result.getTaskName()) && result.getScore() != -1.0) {
					// check if task's score is above it's upper limit or under it's lower limit value
					if(configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() < result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is above it's upper limit (" +
								configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() + ")");
						
						return 1.0;
					} else if(configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() > result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is under it's lower limit (" +
								configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() + ")");
						
						return 0.0;
					}
				}
			}
		} catch(Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while checking task safety limits with method takeFirst");
		}
		
		return -1.0;
	}
	
	/**
	* Checks for each task result if the score is above it's upper safety limit or
	* under it's lower safety limit and selects the exceedance which's corresponding 
	* task has the highest confidence as result.
	* @param taskResults	list of all TaskResult objects
	* @return				the safety limit result,
	* 						0.0 if exceedance with highest task confidence is an exceedance of lower limit,
	* 						1.0 if exceedance with highest task confidence is an exceedance of upper limit,
	* 						-1.0 if no exceedance was found or an error occurred
	*/
	public double takeHighestConfidence(List<TaskResult> taskResults) {
		// list to store TaskResult objects, which score is above it's upper limit or under it's lower limit
		List<TaskResult> aboveUpper = new ArrayList<TaskResult>();
		List<TaskResult> underLower = new ArrayList<TaskResult>();
		
		try {
			LOGGER.debug("Checking task safety limits with method takeHighestConfidence");
			// iterate over each TaskResult and compare the task's score with safety limits from configuration
			for(TaskResult result : taskResults) {
				// check if configuration contains an entry for that task and score is != -1.0
				if(configuration.getTasks().containsKey(result.getTaskName()) && result.getScore() != -1.0) {
					// check if task's score is above it's upper limit or under it's lower limit value
					if(configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() < result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is above it's upper limit (" +
								configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() + ")");
						
						// add TaskResult to aboveUpper list
						aboveUpper.add(result);
					} else if(configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() > result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is under it's lower limit (" +
								configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() + ")");
						
						// add TaskResult to underLower list
						underLower.add(result);
					}
				}
			}
			
			// check if one or both lists are empty
			if(aboveUpper.size() > 0 && underLower.size() == 0) {
				return 1.0;
			} else if (aboveUpper.size() == 0 && underLower.size() > 0) {
				return 0.0;
			} else if(aboveUpper.size() == 0 && underLower.size() == 0) {
				return -1.0;
			} else {
				// choose tasks with highest confidence from both lists
				TaskResult upperResult = aboveUpper.stream().max((entry1, entry2) 
						-> Double.compare(configuration.getTasks().get(entry1.getTaskName()).getConfidence(), 
								configuration.getTasks().get(entry2.getTaskName()).getConfidence())).get();
				TaskResult lowerResult = underLower.stream().min((entry1, entry2) 
						-> Double.compare(configuration.getTasks().get(entry1.getTaskName()).getConfidence(), 
								configuration.getTasks().get(entry2.getTaskName()).getConfidence())).get();
				
				// check which task has higher confidence
				if(configuration.getTasks().get(upperResult.getTaskName()).getConfidence() < 
					configuration.getTasks().get(lowerResult.getTaskName()).getConfidence()) {
					return 0.0;
				} else {
					return 1.0;
				}
			}
			
		} catch(Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while checking task safety limits with method takeHighestConfidence");
		}
		
		return -1.0;
	}
	
	/**
	* Checks for each task result if the score is above it's upper safety limit or
	* under it's lower safety limit and selects the majority of all exceedance of
	* safety limits as result.
	* @param taskResults	list of all TaskResult objects
	* @return				the safety limit result,
	* 						0.0 if there are more exceedances of lower limits than exceedances of upper limits,
	* 						1.0 if there are more exceedances of upper limits than exceedances of lower limits,
	* 						-1.0 if no exceedance was found or an error occurred
	*/
	public double takeMajority(List<TaskResult> taskResults) {
		// list to store TaskResult objects, which score is above it's upper limit or under it's lower limit
		List<TaskResult> aboveUpper = new ArrayList<TaskResult>();
		List<TaskResult> underLower = new ArrayList<TaskResult>();
		
		try {
			LOGGER.debug("Checking task safety limits with method takeMajority");
			// iterate over each TaskResult and compare the task's score with safety limits from configuration
			for(TaskResult result : taskResults) {
				// check if configuration contains an entry for that task and score is != -1.0
				if(configuration.getTasks().containsKey(result.getTaskName()) && result.getScore() != -1.0) {
					// check if task's score is above it's upper limit or under it's lower limit value
					if(configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() < result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is above it's upper limit (" +
								configuration.getTasks().get(result.getTaskName()).getUpperSafetyLimit() + ")");
						
						// add TaskResult to aboveUpper list
						aboveUpper.add(result);
					} else if(configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() > result.getScore()) {
						LOGGER.debug("Score (" + result.getScore() + ") of task " + result.getTaskName() + " is under it's lower limit (" +
								configuration.getTasks().get(result.getTaskName()).getLowerSafetyLimit() + ")");
						
						// add TaskResult to underLower list
						underLower.add(result);
					}
				}
			}
			
			// check if both lists are empty
			if(aboveUpper.size() == 0 && underLower.size() == 0) {
				return -1.0;
			} else if(aboveUpper.size() > underLower.size()) {
				return 1.0;
			} else if(aboveUpper.size() < underLower.size()) {
				return 0.0;
			} else {	// both list have same number of entries --> take highest confidence
				// choose tasks with highest confidence from both lists
				TaskResult upperResult = aboveUpper.stream().max((entry1, entry2) 
						-> Double.compare(configuration.getTasks().get(entry1.getTaskName()).getConfidence(), 
								configuration.getTasks().get(entry2.getTaskName()).getConfidence())).get();
				TaskResult lowerResult = underLower.stream().min((entry1, entry2) 
						-> Double.compare(configuration.getTasks().get(entry1.getTaskName()).getConfidence(), 
								configuration.getTasks().get(entry2.getTaskName()).getConfidence())).get();
				
				// check which task has higher confidence
				if(configuration.getTasks().get(upperResult.getTaskName()).getConfidence() < 
					configuration.getTasks().get(lowerResult.getTaskName()).getConfidence()) {
					return 0.0;
				} else {
					return 1.0;
				}
			}
			
		} catch(Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while checking task safety limits with method takeMajority");
		}
		
		return -1.0;
	}
}
