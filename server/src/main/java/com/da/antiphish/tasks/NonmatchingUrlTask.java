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
 * This task checks if the website contains at least one link (<a> tag) where the text of the link is an URL which is 
 * different to the URL in the href-attribute.
 *  
 * @author Marco Madritsch
 */
public class NonmatchingUrlTask extends Task {
	private Document htmlDoc;
	
	/**
	* Constructor which gets the Document object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	*/
	public NonmatchingUrlTask(Document htmlDoc) {
		this.LOGGER = LoggerFactory.getLogger(NonmatchingUrlTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
	}
	
	/**
	* Checks if the website contains at least one link (<a> tag) where the text of the link is an URL which is 
	* different to the URL in the href-attribute.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null) {
			int count = 0;
			
			try {
				// get all links of website
				Elements linkTags = HtmlHelper.extractLinkTags(htmlDoc);
				
				// check for null
				if(linkTags == null) {
					LOGGER.warn("Extraction of link tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// compare for each link the text with the content of href attribute
				for(Element linkTag : linkTags) {
					URL linkTextUrl;
					URL linkHrefUrl;
					
					// new URL() works only in case of a valid URL, otherwise a malformed URL exception will be thrown
					try {
						linkTextUrl = new URL(linkTag.text());
						linkHrefUrl = new URL(linkTag.attr("href"));
					} catch(MalformedURLException e) {
						linkTextUrl = null;
						linkHrefUrl = null;
					}
					
					// compare urls and increase count if domain of urls is different
					if(linkTextUrl != null && linkHrefUrl != null && !linkTextUrl.getHost().equals(linkHrefUrl.getHost())) {
						LOGGER.debug("Found nonmatching URL \"" + linkTextUrl.toString() + "\" and \"" + linkHrefUrl + "\"");
						count++;
					}
				}
				
				// check if there is at least one nonmatching url link and return result
				if(count > 0) {
					LOGGER.debug("Found " + count + " nonmatching URL(s)");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Did not find any nonmatching URLs");
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
