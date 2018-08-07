package com.da.antiphish.analyse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.configuration.ConfigurationHandler;
import com.da.antiphish.lists.ListsHandler;
import com.da.antiphish.tasks.BlacklistTask;
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
import com.da.antiphish.tasks.TaskResult;
import com.da.antiphish.tasks.SuspiciousScriptContentTask;
import com.da.antiphish.tasks.SuspiciousLinksTask;
import com.da.antiphish.tasks.SuspiciousUrlTask;
import com.da.antiphish.tasks.Task;
import com.da.antiphish.tasks.TitleTask;
import com.da.antiphish.tasks.WebsiteIdentityTask;
import com.da.antiphish.tasks.WhitelistTask;
import com.da.antiphish.types.TrafficLight;

/**
 * Computer represents the controller of an analysis. It gets the input URL from the REST request, handles the analysis 
 * and returns the result.
 * 
 * @author Marco Madritsch
 */
public class Computer {
	private final static Logger LOGGER = LoggerFactory.getLogger(Computer.class);
	private ListsHandler listsHandler;
	private ConfigurationHandler configurationHandler;
	private Map<String, Double> normalisedTaskWeights;
	private ScoreSafetyLimitSelector thresholdScoreSelector;
	private WeightsCalculator weightsCalculator;
	private final ExecutorService executor;
	
	/**
	* Constructor initializes all the necessary objects for an analysis.
	*/
	public Computer() {
		this.listsHandler = new ListsHandler();
		this.configurationHandler = new ConfigurationHandler();
		this.thresholdScoreSelector = new ScoreSafetyLimitSelector(configurationHandler.getConfiguration());
		this.weightsCalculator = new WeightsCalculator();
		this.normalisedTaskWeights = 
				this.weightsCalculator.normaliseTaskEffects(configurationHandler.getConfiguration().getTasks());
		this.executor = Executors.newFixedThreadPool(50);
		
		// set task thresholds based on config values
		DomainAgeTask.setDomainAgeThreshold(configurationHandler.getConfiguration().getDomainAgeThreshold());
		DotsTask.setDotsThreshold(configurationHandler.getConfiguration().getDotsThreshold());
		EmptyLinksTask.setEmptyLinksThreshold(configurationHandler.getConfiguration().getEmptyLinksThreshold());
		PunctuationTask.setPunctuationThreshold(configurationHandler.getConfiguration().getPunctuationThreshold());
		
		LOGGER.info("Computer initialisation done");
	}
	
	/**
	* Analyses the website of the given URL. The final score is the sum of all weighted scores of the TaskResult 
	* objects.
	* @param stringUrl		the URL of the website
	* @return				the final score of the analysis as double between 0.0 and 1.0 if success, -1.0 otherwise
	*/
	public double analyse(String stringUrl) {
		URL url;
		Future<TaskResult> resultFuture;
		TaskResult listTaskResult;
		
		try {
			url = new URL(stringUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return -1.0;
		}
		
		// check if url is in blacklist
		resultFuture = executor.submit(new BlacklistTask(url, listsHandler.getBlacklist()));
		listTaskResult = null;
		
		try {
			listTaskResult = resultFuture.get();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn("Failure while checking blacklist");
		}
		
		if(listTaskResult != null && listTaskResult.getScore() == 1.0) {
			return 1.0;
		}
		
		// check if url is in whitelist
		resultFuture = executor.submit(new WhitelistTask(url, listsHandler.getWhitelist()));
		listTaskResult = null;
		
		try {
			listTaskResult = resultFuture.get();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn("Failure while checking whitelist");
		}
		
		if(listTaskResult != null && listTaskResult.getScore() == 1.0) {
			return 0.0;
		}
		
		// get html content of the given url
		Document htmlDoc = HtmlHelper.getUrlContent(url.toString());
		
		// check if content is null
		if(htmlDoc == null) {
			return -1.0;
		}
		
		// list to hold the Future objects associated with tasks
        List<Task> tasks = new ArrayList<Task>();
        
        // create task instances and add it to tasks list
        tasks.add(new DomainAgeTask(url));
        tasks.add(new DotsTask(url));
        tasks.add(new EmptyLinksTask(htmlDoc));
        tasks.add(new FormTagsTask(htmlDoc, url, listsHandler.getFormlist()));
        tasks.add(new FrameTask(htmlDoc, url));
        tasks.add(new InputTagsTask(htmlDoc));
        tasks.add(new IPAddressTask(url));
        tasks.add(new MetaTagsTask(htmlDoc, listsHandler.getMetalist()));
        tasks.add(new NoLinksBodyTask(htmlDoc));
        tasks.add(new NonmatchingUrlTask(htmlDoc));
        tasks.add(new OnlyScriptTask(htmlDoc));
        tasks.add(new PlaintextContentTask(htmlDoc, listsHandler.getContentlist()));
        tasks.add(new PunctuationTask(url));
        tasks.add(new SuspiciousLinksTask(htmlDoc));
        tasks.add(new SuspiciousScriptContentTask(htmlDoc, listsHandler.getScriptlist()));
        tasks.add(new SuspiciousUrlTask(url));
        tasks.add(new TitleTask(htmlDoc, url));
        tasks.add(new WebsiteIdentityTask(htmlDoc, url));
        
        // list of all TaskResult objects
        List<TaskResult> taskResults = new ArrayList<TaskResult>();
        
        try {
        	// invoke all tasks
	        List<Future<TaskResult>> resultFutures = executor.invokeAll(tasks);
	        
	        // collect results
	        for(Future<TaskResult> future : resultFutures){
	            try {
	                taskResults.add(future.get());
	            } catch (Exception e) {
	                e.printStackTrace();
	                LOGGER.warn(e.getClass().getSimpleName() + " while trying to get future result of a task");
	            }
	        }
	        
	        // calculate final score based on list of TaskResult objects
			double finalScore = calculateFinalScore(taskResults);
	        
			return finalScore;
			
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn(e.getClass().getSimpleName() + " while invoking all tasks");
        }
        
        return -1.0;
	}
	
	/**
	* Calculates the final score of the analysis. The final score is the sum of all weighted scores of the TaskResult 
	* objects.
	* @param taskResults	the TaskResult objects
	* @return				the final score of the analysis as double between 0.0 and 1.0
	*/
	private double calculateFinalScore(List<TaskResult> taskResults) {
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
		double thresholdCheckResult = thresholdScoreSelector.takeMajority(taskResults);
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
	
	/**
	* Returns the traffic light based on the given final score and the defined ranges in configuration file config.json.
	* @param finalScore	the final score of the analysis
	* @return			the corresponding traffic light
	*/
	public TrafficLight getTrafficLight(double finalScore) {
		if(configurationHandler.getConfiguration().getRanges().get("green").contains(finalScore)) {
			return TrafficLight.GREEN;
		} else if(configurationHandler.getConfiguration().getRanges().get("yellow").contains(finalScore)) {
			return TrafficLight.YELLOW;
		} else if(configurationHandler.getConfiguration().getRanges().get("red").contains(finalScore)) {
			return TrafficLight.RED;
		} else {
			return TrafficLight.GRAY;
		}
	}
}
