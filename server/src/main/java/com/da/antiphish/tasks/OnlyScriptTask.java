package com.da.antiphish.tasks;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the body of the website is empty but there are script tags available.
 *  
 * @author Marco Madritsch
 */
public class OnlyScriptTask extends Task {
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public OnlyScriptTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(OnlyScriptTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the body of the website is empty but there are script tags available.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null) {
			try {
				
				// get body of website
				Element htmlBody = htmlDoc.body();
				
				// get all script tags of website
				Elements scriptTags = HtmlHelper.extractScriptTags(htmlDoc);
				
				// check for null
				if(scriptTags == null) {
					LOGGER.warn("Extraction of script tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// check if body is empty but script tags are available
				if((htmlBody == null || htmlBody.children().size() == 0) && scriptTags.size() > 0) {
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}				
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing no body only script tags");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
