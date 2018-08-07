package com.da.antiphish.tasks;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if at least one word of the title of the website also occurs in the domain name of the URL.
 *  
 * @author Marco Madritsch
 */
public class TitleTask extends Task {
	private Document htmlDoc;
	private URL url;
	
	/**
	* Constructor which gets the Document object and the URL object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param url		the URL object of the website to be analysed
	*/
	public TitleTask(Document htmlDoc, URL url) {
		this.LOGGER = LoggerFactory.getLogger(TitleTask.class);
		this.category = Category.METADATA;
		this.htmlDoc = htmlDoc;
		this.url = url;
	}
	
	/**
	* Checks if at least one word of the title of the website also occurs in the domain name of the URL or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && url != null) {
			try {
				// get title of website
				String title = htmlDoc.title();
				
				if(title != "") {
					// split title in single words
					String[] titleWords = title.split("[\\W_]+");
					
					// check if domain contains at least one word of title
					for(String word : titleWords) {
						if(word != "" && (StringUtils.containsIgnoreCase(url.getHost(), word) || StringUtils.containsIgnoreCase(word, url.getHost()))) {
							LOGGER.debug("Found title word \"" + word + "\" in URL.");
							return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
						}
					}
				}
				
				LOGGER.debug("Did not find any title word in domain");
				return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				
			} catch (Exception e) {
	        	e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing links");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}
}
