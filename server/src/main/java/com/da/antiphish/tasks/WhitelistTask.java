package com.da.antiphish.tasks;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.da.antiphish.lists.WhitelistEntry;
import com.da.antiphish.types.Category;

/**
 * This task checks if the given URL is in whitelist or not.
 * 
 * @author Marco Madritsch
 */
public class WhitelistTask extends Task {
	private URL url;
	private Map<String, WhitelistEntry> whitelist;
	
	/**
	* Constructor which gets the URL object of a website and the whitelist of the system.
	* @param url		the URL object of the website to be analysed
	* @param whitelist	the whitelist of the system
	*/
	public WhitelistTask(URL url, Map<String, WhitelistEntry> whitelist) {
		this.LOGGER = LoggerFactory.getLogger(WhitelistTask.class);
		this.category = Category.STRUCTURE;
		this.url = url;
		this.whitelist = whitelist;
	}
	
	/**
	* Checks if the given URL is in the whitelist or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null && whitelist != null) {
			try {
				String stringUrl = url.toString();
				
				// check if URL is in whitelist or not and return result
				if(whitelist.values().stream().anyMatch(entry -> StringUtils.containsIgnoreCase(stringUrl, entry.getUrl()))) {
					LOGGER.debug("Found URL \"" + url.toString() + "\" in whitelist");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Did not find URL \"" + url.toString() + "\" in whitelist");
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
