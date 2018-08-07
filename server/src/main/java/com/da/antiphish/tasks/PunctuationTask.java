package com.da.antiphish.tasks;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if the number of punctuations (. ! # $ % & , ; ') in the URL is greater than a specified threshold.
 * 
 * @author Marco Madritsch
 */
public class PunctuationTask extends Task {
	private final String[] punctuationMarks = new String[]{".", "!", "#", "$", "%", "&", ",", ";", "'"};
	private static int punctuationThreshold = 23;	// threshold for number of punctuation marks (default value)
	private URL url;
	
	/**
	* Constructor which gets the URL object of a website.
	* @param url	the URL object of the website to be analysed
	*/
	public PunctuationTask(URL url) {
		this.LOGGER = LoggerFactory.getLogger(PunctuationTask.class);
		this.category = Category.URL;
		this.url = url;
	}
	
	/**
	* Checks if the number of punctuations (. ! # $ % & , ; ') in the URL is greater than the specified threshold
	* (punctuationThreshold).
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null) {
			try {
				String stringUrl = url.toString();
				
				// count number of punctuation marks
				int punctuationCount = 0;
				for(int i = 0; i < punctuationMarks.length; i++) {
					punctuationCount += StringUtils.countMatches(stringUrl, punctuationMarks[i]);
				}
				
				LOGGER.debug(punctuationCount + " punctuation marks of max " + punctuationThreshold + " in URL \"" 
						+ url.toString() + "\"");
				
				// check if number of parameters is above threshold and return result
				if(punctuationCount > punctuationThreshold) {
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0, (double)punctuationCount);
				} else {
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0, (double)punctuationCount);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing URL \"" + url.toString() + "\"");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}
	
	/**
	* Sets the punctuation threshold.
	* @param punctuationThreshold	the new punctuation threshold
	*/
	public static void setPunctuationThreshold(int punctuationThreshold) {
		PunctuationTask.punctuationThreshold = punctuationThreshold;
	}
}
