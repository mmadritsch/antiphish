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
 * This task checks if the website contains suspicious frames or not. A frame is considered as suspicious if the domain 
 * of the URL in the src-attribute has no relationship to the actual domain (equal or subdomain-relationship).
 *  
 * @author Marco Madritsch
 */
public class FrameTask extends Task {
	private Document htmlDoc;
	private URL url;
	
	/**
	* Constructor which gets the Document object and the URL object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param url		the URL object of the website to be analysed
	*/
	public FrameTask(Document htmlDoc, URL url) {
		this.LOGGER = LoggerFactory.getLogger(FrameTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
		this.url = url;
	}
	
	/**
	* Checks if the website contains at least one suspicious frame or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && url != null) {
			int count = 0;
			
			try {
				// get all frames of website
				Elements frameTags = HtmlHelper.extractFrameTags(htmlDoc);
				
				// check for null
				if(frameTags == null) {
					LOGGER.warn("Extraction of iframe tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// check if there is at least one iframe where the src-attribute contains an url which domain is not
				// equal to actual domain
				for(Element frameTag : frameTags) {
					URL srcURL;
					
					// new URL() works only in case of a valid URL, otherwise a malformed URL exception will be thrown
					try {
						srcURL = new URL(frameTag.attr("src"));
					} catch(MalformedURLException e) {
						srcURL = null;
					}
					
					// check for null
					if(srcURL != null) {
						// remove subdomain www. if available in order to be able to check subdomain relationship
						String srcDomain = srcURL.getHost();
						if(srcDomain.startsWith("www.")) {
							srcDomain = srcDomain.substring(4);
						}
						
						String urlDomain = url.getHost();
						if(urlDomain.startsWith("www.")) {
							urlDomain = urlDomain.substring(4);
						}
						
						// compare urls and increase count if domain of urls is different or there is no subdomain relationship
						if(!srcDomain.endsWith(urlDomain) && !urlDomain.endsWith(srcDomain)) {
							LOGGER.debug("Found suspicious frame with src domain \"" + srcDomain + "\"");
							count++;
						}
					}
				}
				
				// check if there is at least one suspicious frame
				if(count > 0) {
					LOGGER.debug("Found " + count + " suspicious frame(s)");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Did not find any suspicious frames");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing frames");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
