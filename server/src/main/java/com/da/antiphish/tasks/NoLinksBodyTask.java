package com.da.antiphish.tasks;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the website contains input fields (<input> tags) but no links.
 *  
 * @author Marco Madritsch
 */
public class NoLinksBodyTask extends Task {
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public NoLinksBodyTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(NoLinksBodyTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the website contains input fields (<input> tags) but no links.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null) {
			try {
				// get all input tags of website
				Elements inputTags = HtmlHelper.extractInputTags(htmlDoc);
				
				// get all links of website
				Elements linkTags = HtmlHelper.extractLinkTags(htmlDoc);
				
				// check for null
				if(linkTags == null || inputTags == null) {
					LOGGER.warn("Extraction of link or input tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// check if there is at least one input tag but no link
				if(inputTags.size() > 0 && linkTags.size() == 0) {
					LOGGER.debug("No links but input tags found in body");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Found " + linkTags.size() + " link(s) and " + inputTags.size() + " input tag(s) in body");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing links");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
