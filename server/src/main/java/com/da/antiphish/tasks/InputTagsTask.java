package com.da.antiphish.tasks;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the website contains input fields (<input> tags) or not.
 *  
 * @author Marco Madritsch
 */
public class InputTagsTask extends Task {
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public InputTagsTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(InputTagsTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the website contains at least one input tag or not. 
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		// check if htmlDoc is not null
		if(htmlDoc != null) {
			try {
				// get all input tags of website
				Elements inputTags = HtmlHelper.extractInputTags(htmlDoc);
				
				// check for null
				if(inputTags == null) {
					LOGGER.warn("Extraction of input tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// check if there is at least one input tag or not
				if(inputTags.size() == 0) {
					LOGGER.debug("No input tags found");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				} else {
					LOGGER.debug("Found " + inputTags.size() + " input tag(s)");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing links");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
