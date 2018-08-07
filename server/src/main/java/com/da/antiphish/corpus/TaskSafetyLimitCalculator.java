package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.configuration.ConfigurationHandler;
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
 * TaskSafetyLimitCalculator calculates the lower and upper safety limit of each task based on the specified corpus 
 * analysis JSON file and stores the result in an own JSON file. This result has to be manually entered into 
 * config.json file.
 * 
 * @author Marco Madritsch
 */
public class TaskSafetyLimitCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskSafetyLimitCalculator.class);
//	private static final String corpusAnalysisPath = 
//			"C:/Users/Marco/Documents/Corpus/2017-11-29/training/training-analysis_2017-12-02.json";
	private static final String corpusAnalysisPath = 
			"C:/Users/Marco/Documents/Corpus/2017-11-29_new/training/training-analysis_2017-12-10.json";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();
	
	/**
	 * Reads first all corpus analysis results out of the specified json file (path in variable corpusAnalysisPath) 
	 * and stores them in a list of CorpusAnalysisEntry objects. Calculates afterwards the safety limits based on a
	 * specified accuracy (variable accuracy) for all tasks and saves the results in an own JSON file (specified path 
	 * in variable resultPath).
	 */
	public static void main(String[] args) {
		// accuracy
        double accuracy = 0.99;
        
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
    		
    		// calculate upper and lower limit
        	double upperLimit = calculateUpperLimit(corpusAnalysisList, taskName, accuracy);
        	double lowerLimit = calculateLowerLimit(corpusAnalysisList, taskName, upperLimit, accuracy);
        	LOGGER.debug(taskName + ": upper limit " + upperLimit + ", lower limit " + lowerLimit);
        	
        	// put task's safety limits to json result object
        	innerNode.put("upperLimit", upperLimit);
        	innerNode.put("lowerLimit", lowerLimit);
        	node.set(taskName, innerNode);
        }
        
        // save safety limits as json file
        try {
			mapper.writeValue(new File(resultPath + "/taskSafetyLimits.json"), node);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving safety limits to json file");
		}
	}
	
	/**
	 * Calculates the upper safety limit of a given task according to the specified accuracy.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @param taskName				the name of the task which's upper safety limit should be calculated
	 * @param minAccuracy			the minimum accuracy with which the limit should be calculated
	 * @return						the upper safety limit as double
	 */
	public static double calculateUpperLimit(List<CorpusAnalysisEntry> corpusAnalysisList, String taskName, 
			double minAccuracy) {
		int total;
		int errors;
		
		for(double current = 0.0; current < 1.0; current += 0.01) {
			LOGGER.debug("Checking upper limit: " + current);
			total = 0;
			errors = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
	        	// check if task was successful, otherwise skip it
	        	if(entry.getTaskResults().get(taskName).get("score") != -1.0) {	        		
	        		// check if result > current
	            	if(entry.getTaskResults().get(taskName).get("score") > current) {
	            		// increase total count
	            		total++;
            			
            			// check if it was wrong labeled
	            		if((!entry.getPhishing() && (entry.getTaskResults().get(taskName).get("score") > 
	            				configurationHandler.getConfiguration().getTasks().get(taskName).getThreshold())) || 
	            				(entry.getPhishing() && (entry.getTaskResults().get(taskName).get("score") <= 
	            				configurationHandler.getConfiguration().getTasks().get(taskName).getThreshold()))) {
	            			errors++;
	            		}
	            	}
	        	}
			}
	        
	        // check if upper limit was found and return it
			if(total > 0) {
				if(((double)errors/total) > (1.0-minAccuracy)) {
					LOGGER.debug("Too high error rate: " + errors + "/" + total + "=" + (double)errors/total);
				} else {
					LOGGER.debug("Found upper limit: " + current + ", error rate: " + errors + "/" + total + "=" 
							+ (double)errors/total);
					return current;
				}
			} else {
				LOGGER.debug("Found upper limit: " + current + ", error rate: " + errors + "/" + total + "= 0");
				return current;
			}
		}
		
		// no upper limit found, return default = max
		return 1.0;
	}
	
	/**
	 * Calculates the lower safety limit of a given task according to the specified accuracy.
	 * @param corpusAnalysisList	the list of all CorpusAnalysisEntry objects
	 * @param taskName				the name of the task which's lower safety limit should be calculated
	 * @param upperLimit			the calculated upper safety limit of that task
	 * @param minAccuracy			the minimum accuracy with which the limit should be calculated
	 * @return						the lower safety limit as double
	 */
	public static double calculateLowerLimit(List<CorpusAnalysisEntry> corpusAnalysisList, String taskName, 
			double upperLimit, double minAccuracy) {
		int errors;
		int total;
		
		for(double current = upperLimit; current > 0.0; current -= 0.01) {
			LOGGER.debug("Checking lower limit: " + current);
			errors = 0;
			total = 0;
			
			// iterate over each corpus analysis result (= all analysed websites)
			for(CorpusAnalysisEntry entry : corpusAnalysisList) {
	        	// check if task was successful, otherwise skip it
	        	if(entry.getTaskResults().get(taskName).get("score") != -1.0) {	        		
	        		// check if result < current
	            	if(entry.getTaskResults().get(taskName).get("score") < current) {
	            		// increase total count
	            		total++;
            			
            			// check if it was wrong labeled
	            		if(!entry.getPhishing() && (entry.getTaskResults().get(taskName).get("score") > 
	            				configurationHandler.getConfiguration().getTasks().get(taskName).getThreshold()) || 
	            				(entry.getPhishing() && entry.getTaskResults().get(taskName).get("score") <= 
	            				configurationHandler.getConfiguration().getTasks().get(taskName).getThreshold())) {
	            			errors++;
	            		}
	            	}
	        	}
			}
	        
	        // check if lower limit was found and return it
			if(total > 0) {
				if(((double)errors/total) > (1.0-minAccuracy)) {
					LOGGER.debug("Too high error rate: " + errors + "/" + total + "=" + (double)errors/total);
				} else {
					LOGGER.debug("Found lower limit: " + current + ", error rate: " + errors + "/" + total + "=" 
							+ (double)errors/total);
					return current;
				}
			} else {
				LOGGER.debug("Found lower limit: " + current + ", error rate: " + errors + "/" + total + "= 0");
				return current;
			}
		}
		
		// no lower limit found, return default = max
		return 0.0;
	}
}
