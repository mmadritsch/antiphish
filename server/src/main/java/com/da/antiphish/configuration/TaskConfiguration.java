package com.da.antiphish.configuration;

/**
 * TaskConfiguration includes all the necessary settings for a single analysis task which are stored
 * in the file config.json. Includes the following information:
 * <ul>
 * <li>confidence: the confidence of the task</li>
 * <li>truePositive: the true positive rate of the task</li>
 * <li>trueNegative: the true negative rate of the task</li>
 * <li>lowerSafetyLimit: the lower safety limit of the task</li>
 * <li>upperSafetyLimit: the upper safety limit of the task</li>
 * <li>threshold: the threshold of the task</li>
 * </ul>
 * 
 * @author Marco Madritsch
 */
public class TaskConfiguration {
	private Double confidence;
	private Double truePositive;
	private Double trueNegative;
	private Double lowerSafetyLimit;
	private Double upperSafetyLimit;
	private Double threshold;
	
	/**
	* Default constructor with no arguments.
	*/
	public TaskConfiguration() {}
	
	/**
	* Constructor with all possible arguments.
	* @param confidence			the confidence of the task
	* @param truePositive		the true positive rate of the task
	* @param trueNegative		the true negative rate of the task
	* @param lowerSafetyLimit	the lower safety limit of the task
	* @param upperSafetyLimit	the upper safety limit of the task
	* @param threshold			the threshold of the task
	*/
	public TaskConfiguration(Double confidence, Double truePositive, Double trueNegative, Double lowerSafetyLimit, 
			Double upperSafetyLimit, Double threshold) {
		this.confidence = confidence;
		this.truePositive = truePositive;
		this.trueNegative = trueNegative;
		this.lowerSafetyLimit = lowerSafetyLimit;
		this.upperSafetyLimit = upperSafetyLimit;
		this.threshold = threshold;
	}
	
	/**
	* Gets the confidence of the task.
	* @return the confidence of the task
	*/
	public Double getConfidence() {
		return confidence;
	}
	
	/**
	* Sets the confidence of the task.
	* @param confidence	the confidence of the task
	*/
	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	
	/**
	* Gets the true positive rate of the task.
	* @return the true positive rate of the task
	*/
	public Double getTruePositive() {
		return truePositive;
	}
	
	/**
	* Sets the true positive rate of the task.
	* @param truePositive	the true positive rate of the task
	*/
	public void setTruePositive(Double truePositive) {
		this.truePositive = truePositive;
	}
	
	/**
	* Gets the true negative rate of the task.
	* @return the true negative rate of the task
	*/
	public Double getTrueNegative() {
		return trueNegative;
	}
	
	/**
	* Sets the true negative rate of the task.
	* @param trueNegative	the true negative rate of the task
	*/
	public void setTrueNegative(Double trueNegative) {
		this.trueNegative = trueNegative;
	}
	
	/**
	* Gets the lower safety limit of the task.
	* @return the lower safety limit of the task
	*/
	public Double getLowerSafetyLimit() {
		return lowerSafetyLimit;
	}
	
	/**
	* Sets the lower safety limit of the task.
	* @param lowerSafetyLimit	the lower safety limit of the task
	*/
	public void setLowerSafetyLimit(Double lowerSafetyLimit) {
		this.lowerSafetyLimit = lowerSafetyLimit;
	}
	
	/**
	* Gets the upper safety limit of the task.
	* @return the upper safety limit of the task
	*/
	public Double getUpperSafetyLimit() {
		return upperSafetyLimit;
	}
	
	/**
	* Sets the upper safety limit of the task.
	* @param upperSafetyLimit	the upper safety limit of the task
	*/
	public void setUpperSafetyLimit(Double upperSafetyLimit) {
		this.upperSafetyLimit = upperSafetyLimit;
	}
	
	/**
	* Gets the threshold of the task.
	* @return the threshold of the task
	*/
	public Double getThreshold() {
		return threshold;
	}
	
	/**
	* Sets the threshold of the task.
	* @param threshold	the threshold of the task
	*/
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
}