package com.da.antiphish.configuration;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;

/**
 * Configuration represents all the necessary settings for the server which are stored in the file config.json. 
 * Includes the following information:
 * <ul>
 * <li>overallThreshold: threshold (final score) up to which a website is classified as legitimate and above which as 
 * 		phishing site</li>
 * <li>domainAgeThreshold: threshold of the domain age for DomainAgeTask</li>
 * <li>dotsThreshold: threshold of number of dots for DotsTask</li>
 * <li>emptyLinksThreshold: threshold of proportion of empty links for EmptyLinksTask</li>
 * <li>punctuationThreshold: threshold of number of punctuations for PunctuationTask</li>
 * <li>ranges: map of ranges of traffic lights (green, yellow, red)</li>
 * <li>tasks: map of TaskConfiguration objects of all analysis tasks</li>
 * </ul>
 * 
 * @author Marco Madritsch
 *
 */
public class Configuration {
	private Double overallThreshold;
	private Integer domainAgeThreshold;
	private Integer dotsThreshold;
	private Double emptyLinksThreshold;
	private Integer punctuationThreshold;
	private Map<String, Range<Double>> ranges;
	private Map<String, TaskConfiguration> tasks;
	
	/**
	* Constructor with no arguments instantiates the ranges and tasks hashmaps.
	*/
	public Configuration() { 
		this.ranges = new HashMap<String, Range<Double>>();
		this.tasks = new HashMap<String, TaskConfiguration>();
	}
	
	/**
	* Constructor with all possible arguments.
	* @param overallThreshold 		threshold (final score) up to which a website is classified as 
	* 								legitimate and above which as phishing site
	* @param domainAgeThreshold		threshold of the domain age for DomainAgeTask
	* @param dotsThreshold	 		threshold of number of dots for DotsTask
	* @param emptyLinksThreshold 	threshold of proportion of empty links for EmptyLinksTask
	* @param punctuationThreshold	threshold of number of punctuations for PunctuationTask
	* @param ranges					map of ranges of traffic lights (green, yellow, red)
	* @param tasks					map of TaskConfiguration objects of all analysis tasks
	*/
	public Configuration(Double overallThreshold, Integer domainAgeThreshold, Integer dotsThreshold, 
			Double emptyLinksThreshold, Integer punctuationThreshold, Map<String, Range<Double>> ranges, 
			Map<String, TaskConfiguration> tasks) {
		this.overallThreshold = overallThreshold;
		this.domainAgeThreshold = domainAgeThreshold;
		this.dotsThreshold = dotsThreshold;
		this.emptyLinksThreshold = emptyLinksThreshold;
		this.punctuationThreshold = punctuationThreshold;
		this.ranges = ranges;
		this.tasks = tasks;
	}
	
	/**
	* Gets the overall threshold of the configuration.
	* @return the overall threshold of the configuration
	*/
	public Double getOverallThreshold() {
		return overallThreshold;
	}
	
	/**
	* Sets the overall threshold of the configuration.
	* @param overallThreshold	the overall threshold of the configuration
	*/
	public void setOverallThreshold(Double overallThreshold) {
		this.overallThreshold = overallThreshold;
	}
	
	/**
	* Gets the map of ranges of the configuration.
	* @return the map of ranges of the configuration
	*/
	public Map<String, Range<Double>> getRanges() {
		return ranges;
	}
	
	/**
	* Sets the map of ranges of the configuration.
	* @param ranges	the map of ranges of the configuration
	*/
	public void setRanges(Map<String, Range<Double>> ranges) {
		this.ranges = ranges;
	}
	
	/**
	* Gets the map of tasks (TaskConfiguration objects) of the configuration.
	* @return the map of tasks of the configuration
	*/
	public Map<String, TaskConfiguration> getTasks() {
		return tasks;
	}
	
	/**
	* Sets the map of tasks (TaskConfiguration objects) of the configuration.
	* @param tasks	the map of tasks of the configuration
	*/
	public void setTasks(Map<String, TaskConfiguration> tasks) {
		this.tasks = tasks;
	}
	
	/**
	* Gets the domain age threshold of the configuration.
	* @return the domain age threshold of the configuration
	*/
	public Integer getDomainAgeThreshold() {
		return domainAgeThreshold;
	}
	
	/**
	* Sets the domain age threshold of the configuration.
	* @param domainAgeThreshold	the domain age threshold of the configuration
	*/
	public void setDomainAgeThreshold(Integer domainAgeThreshold) {
		this.domainAgeThreshold = domainAgeThreshold;
	}
	
	/**
	* Gets the dots threshold of the configuration.
	* @return the dots threshold of the configuration
	*/
	public Integer getDotsThreshold() {
		return dotsThreshold;
	}
	
	/**
	* Sets the dots threshold of the configuration.
	* @param dotsThreshold	the dots threshold of the configuration
	*/
	public void setDotsThreshold(Integer dotsThreshold) {
		this.dotsThreshold = dotsThreshold;
	}
	
	/**
	* Gets the empty links threshold of the configuration.
	* @return the empty links threshold of the configuration
	*/
	public Double getEmptyLinksThreshold() {
		return emptyLinksThreshold;
	}
	
	/**
	* Sets the empty links threshold of the configuration.
	* @param emptyLinksThreshold	the empty links threshold of the configuration
	*/
	public void setEmptyLinksThreshold(Double emptyLinksThreshold) {
		this.emptyLinksThreshold = emptyLinksThreshold;
	}
	
	/**
	* Gets the punctuation threshold of the configuration.
	* @return the punctuation threshold of the configuration
	*/
	public Integer getPunctuationThreshold() {
		return punctuationThreshold;
	}
	
	/**
	* Sets the punctuation threshold of the configuration.
	* @param punctuationThreshold	the punctuation threshold of the configuration
	*/
	public void setPunctuationThreshold(Integer punctuationThreshold) {
		this.punctuationThreshold = punctuationThreshold;
	}
}