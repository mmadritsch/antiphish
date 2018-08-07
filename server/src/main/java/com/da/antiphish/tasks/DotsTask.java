package com.da.antiphish.tasks;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if the number of dots in the URL is greater than a specified threshold or not.
 * 
 * @author Marco Madritsch
 */
public class DotsTask extends Task {
	private static int dotsThreshold = 1;	// threshold for number of dots (less dot after www and dot before top-level domain) (default value)
	private URL url;
	
	/**
	* Constructor which gets an URL.
	* @param url	the URL of the website to be analysed
	*/
	public DotsTask(URL url) {
		this.LOGGER = LoggerFactory.getLogger(DotsTask.class);
		this.category = Category.URL;
		this.url = url;
	}
	
	/**
	* Checks if the number of dots in the URL is greater than the specified threshold (dotsThreshold) or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null) {
			try {
				String stringUrl = url.toString();
				
				// count dots
				int numberOfDots = StringUtils.countMatches(stringUrl, ".");
				
				// check if url contains "www." --> 2 dots are usual and can be subtracted
				// otherwise subtract only one dot (dot before top-level domain)
				if(stringUrl.contains("www.") && numberOfDots >= 2) {
					numberOfDots = numberOfDots - 2;
					LOGGER.debug("URL \"" + url.toString() + "\" contains \"www.\" and hence 2 dots will be subtracted");
				} else {
					numberOfDots = numberOfDots - 1;
					LOGGER.debug("URL \"" + url.toString() + "\" does not contain \"www.\" and hence 1 dot will be subtracted");
				}
				
				LOGGER.debug(numberOfDots + " dots of max " + dotsThreshold + " in URL \""  + url.toString() + "\"");
				
				// check if number of dots is above threshold and return result
				if(numberOfDots > dotsThreshold) {
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0, (double)numberOfDots);
				} else {
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0, (double)numberOfDots);
				}
				
			} catch (Exception e) {
	        	e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing URL \"" + url.toString() + "\"");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}
	
	/**
	* Sets the dots threshold.
	* @param dotsThreshold	the new dots threshold
	*/
	public static void setDotsThreshold(int dotsThreshold) {
		DotsTask.dotsThreshold = dotsThreshold;
	}
}
