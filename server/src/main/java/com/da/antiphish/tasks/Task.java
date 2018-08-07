package com.da.antiphish.tasks;

import java.util.concurrent.Callable;

import org.slf4j.Logger;

import com.da.antiphish.types.Category;

/**
 * Task is the abstract base class for all single analysis steps.
 * 
 * @author Marco Madritsch
 *
 */
public abstract class Task implements Callable<TaskResult> {
	protected Category category;
	protected Logger LOGGER;
	
	/**
	* Normalises the value x according to the given minimum and maximum values.
	* @param x		value to be normalised
	* @param min	minimum value
	* @param max	maximum value
	* @return		normalised value of x as double
	*/
	public static double normalise(int x, int min, int max) {
		if(x > max) {
			return 1.0;
		} else {
			return (double)(x - min)/(double)(max - min);
		}
	}
}
