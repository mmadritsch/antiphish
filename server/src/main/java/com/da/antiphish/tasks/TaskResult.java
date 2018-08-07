package com.da.antiphish.tasks;

import com.da.antiphish.types.Category;

/**
 * TaskResult represents the result of an analysis task and includes the following information:
 * <ul>
 * <li>taskName: the name of the analysis task</li>
 * <li>category: the category of the analysis task</li>
 * <li>score: the final score of the analysis task</li>
 * <li>addInfo: additional information of specific tasks, only for analysis purposes</li>
 * </ul>
 * 
 * @author Marco Madritsch
 */
public class TaskResult {
	private String taskName;
	private Category category;
	private double score;
	private double addInfo;
	
	/**
	* Constructor with three arguments.
	* @param taskName	the name of the analysis task
	* @param category	the category of the analysis task
	* @param score		the final score of the analysis task
	*/
	public TaskResult(String taskName, Category category, double score) {
		this.taskName = taskName;
		this.category = category;
		this.score = score;
		this.addInfo = -1.0;
	}
	
	/**
	* Constructor with all possible arguments.
	* @param taskName	the name of the analysis task
	* @param category	the category of the analysis task
	* @param score		the final score of the analysis task
	* @param addInfo	additional information of the task, only for analysis purposes
	*/
	public TaskResult(String taskName, Category category, double score, double addInfo) {
		this.taskName = taskName;
		this.category = category;
		this.score = score;
		this.addInfo = addInfo;
	}
	
	/**
	* Gets the name of the analysis task.
	* @return the name of the analysis task
	*/
	public String getTaskName() {
		return this.taskName;
	}
	
	/**
	* Gets the category of the analysis task.
	* @return the category of the analysis task
	*/
	public Category getCategory() {
		return this.category;
	}
	
	/**
	* Gets the score of the analysis task.
	* @return the score of the analysis task
	*/
	public double getScore() {
		return this.score;
	}
	
	/**
	* Gets the additional information of the analysis task.
	* @return the additional information of the analysis task
	*/
	public double getAddInfo() {
		return this.addInfo;
	}
}