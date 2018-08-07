package com.da.antiphish.tasks;

import java.net.URL;

import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if the URL of the website is suspicious. An URL is considered as suspicious if it contains at least 
 * one at sign (@) in the whole URL or one hyphen (-) in the domain of the URL.
 * 
 * @author Marco Madritsch
 */
public class SuspiciousUrlTask extends Task {
	private URL url;
	
	/**
	* Constructor which gets the URL object of a website.
	* @param url		the URL object of the website to be analysed
	*/
	public SuspiciousUrlTask(URL url) {
		this.LOGGER = LoggerFactory.getLogger(SuspiciousUrlTask.class);
		this.category = Category.URL;
		this.url = url;
	}
	
	/**
	* Checks if the URL of the website is suspicious or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null) {
			try {
				String stringUrl = url.toString();
				
				// check if whole URL contains at least one "@" sign or domain name at least one "-" sign and return result
				if(stringUrl.contains("@") || url.getHost().contains("-")) {
					LOGGER.debug("URL/domain name \"" + url.toString() + "\" contains \"@\" or \"-\" signs");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("URL/domain name \"" + url.toString() + "\" does not contain \"@\" or \"-\" signs");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
			} catch (Exception e) {
	        	e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing URL \"" + url.toString() + "\"");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
