package com.da.antiphish.tasks;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.da.antiphish.lists.BlacklistEntry;
import com.da.antiphish.types.Category;

/**
 * This task checks if the given URL is in blacklist or not.
 * 
 * @author Marco Madritsch
 */
public class BlacklistTask extends Task {
	private URL url;
	private Map<String, BlacklistEntry> blacklist;
	
	/**
	* Constructor which gets the URL object of a website and the blacklist of the system.
	* @param url		the URL object of the website to be analysed
	* @param blacklist	the blacklist of the system
	*/
	public BlacklistTask(URL url, Map<String, BlacklistEntry> blacklist) {
		this.LOGGER = LoggerFactory.getLogger(BlacklistTask.class);
		this.category = Category.URL;
		this.url = url;
		this.blacklist = blacklist;
	}
	
	/**
	* Checks if the given URL is in the blacklist or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null && blacklist != null) {
			String stringUrl;
			
			try {
				
				stringUrl = url.toString();
				
				// check if url is in blacklist
				if(blacklist.values().stream().anyMatch(entry -> StringUtils.containsIgnoreCase(stringUrl, 
						entry.getUrl()))) {
					LOGGER.debug("Found URL \"" + url.toString() + "\" in blacklist");
					
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Did not find URL \"" + url.toString() + "\" in blacklist");
					
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
