package com.da.antiphish.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the website contains suspicius links. A link is considered as suspicious if it contains at least 
 * one at sign (@) in the whole link or one hyphen (-) in the domain of the link.
 * 
 * @author Marco Madritsch
 */
public class SuspiciousLinksTask extends Task {
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public SuspiciousLinksTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(SuspiciousLinksTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the website contains at least one suspicius link or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null) {
			int count = 0;
			
			try {
				
				// get all links of the html content
				Elements linkTags = HtmlHelper.extractLinkTags(htmlDoc);
				
				// check for null
				if(linkTags == null) {
					LOGGER.warn("Extraction of link tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// count links which contain at least one "@" in whole link or one
				// "-" in domain name
				for(Element link : linkTags) {
					// try to cast link to an URL object - only possible if it's a valid URL (not a relative path)
					URL url;
					try {
						url = new URL(link.attr("href"));
					} catch(MalformedURLException e){
						url = null;
					}
					
					// check if whole link contains an "@" sign or domain name contains an "-" sign
					if(link.attr("href").contains("@") || (url != null && url.getHost().contains("-"))) {
						count++;
					}
				}
				
				// check if there is at least one suspicious link and return result
				if(count > 0) {
					LOGGER.debug("Found " + count + " suspicious link(s) of total " + linkTags.size() + " link(s)");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Did not find any suspicious links");
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
