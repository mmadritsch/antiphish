package com.da.antiphish.tasks;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the rate of empty links of the website is greater than a specified threshold or not.
 * 
 * @author Marco Madritsch
 */
public class EmptyLinksTask extends Task {
	private static double emptyLinksThreshold = 0.33;	// threshold for empty links rate (default value)
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public EmptyLinksTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(EmptyLinksTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the empty links rate of the website is greater than the specified threshold (emptyLinksThreshold) 
	* or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		
		if(htmlDoc != null) {
			try {
				
				// get all links of the html content
				Elements linkTags = HtmlHelper.extractLinkTags(htmlDoc);
				
				// check for null
				if(linkTags == null) {
					LOGGER.warn("Extraction of link tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// count links which href attribute starts with "#", "javascript:;" or ""
				int count = 0;
				for(Element link : linkTags) {
					if(link.attr("href").startsWith("#") || link.attr("href").toLowerCase().equals("javascript:;") 
							|| link.attr("href").toLowerCase().equals("javascript:void(0);") || link.attr("href").equals("")) {
						count++;
					}
				}
				
				LOGGER.debug("Found " + count + " empty link(s) of total " + linkTags.size() + " links");
				
				// calculate empty links rate
				double emptyLinksRate = 0.0;
				if(linkTags.size() > 0) {
					emptyLinksRate = (double)count/linkTags.size();
				}
				
				if(emptyLinksRate > emptyLinksThreshold) {
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0, emptyLinksRate);
				} else {
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0, emptyLinksRate);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing links");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}
	
	/**
	* Sets the empty links threshold.
	* @param emptyLinksThreshold	the new empty links threshold
	*/
	public static void setEmptyLinksThreshold(double emptyLinksThreshold) {
		EmptyLinksTask.emptyLinksThreshold = emptyLinksThreshold;
	}
}
