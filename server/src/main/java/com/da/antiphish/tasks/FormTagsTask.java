package com.da.antiphish.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the website contains bad forms or not. A form is considered as bad if all the following 
 * conditions are satisfied:
 * - the website contains a form (<form> tag)
 * - there is at least one input field (<input> tag) within the scope of the form
 * - there are suspicious keywords/contents in the scope of the form or there is no plaintext at all but images
 * - HTTPS is not used in the action field if it contains an absolute path, or HTTPS is not used if it is empty or
 *   contains a relative path
 *  
 * @author Marco Madritsch
 */
public class FormTagsTask extends Task {
	private Document htmlDoc;
	private URL url;
	private List<String> formlist;
	
	/**
	* Constructor which gets the Document object and the URL object of a website and the form list of the system.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param url		the URL object of the website to be analysed
	* @param formlist	the list of suspicious form contents of the system
	*/
	public FormTagsTask(Document htmlDoc, URL url, List<String> formlist) {
		this.LOGGER = LoggerFactory.getLogger(FormTagsTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
		this.url = url;
		this.formlist = formlist;
	}
	
	/**
	* Checks if the website contains at least one bad form or not. 
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && url != null) {
			try {
				// get all form tags of website
				Elements formTags = HtmlHelper.extractFormTags(htmlDoc);
				
				// check for null
				if(formTags == null) {
					LOGGER.warn("Extraction of form tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				for(Element formTag : formTags) {
					// get all input tags of that form
					Elements inputTags = formTag.select("input");
					
					// check for null
					if(inputTags != null) {
						// split plain text
						String[] words = formTag.toString().split("[\\W]+");
						
						// filter words of plaintext which are in content list
						List<String> matchingContent = new ArrayList<String>();
						for(int i = 0; i < words.length; i++) {
							if(formlist.contains(words[i].toLowerCase())) {
								matchingContent.add(formlist.get(formlist.indexOf(words[i].toLowerCase())));
							}
						}
						
						// extract whole plaintext and <img> tags from form
						String formText = formTag.text();
						Elements formImages = formTag.select("img");
						
						// check if form contains input tags and
						// 	- if keywords can be found in the scope of the form or
						//	- if there is no plaintext in the scope of the form but image(s)
						if(inputTags.size() > 0 && (matchingContent.size() > 0 || (formText.equals("") && formImages.size() > 0))) {
							LOGGER.debug("Found " + matchingContent.size() + " keyword(s) in form tags");
							
							// extract action field of form
							String action = formTag.attr("action");
							
							// try to cast action field to an URL object - only possible if it's a valid URL (not a relative path)
							URL actionUrl;
							try {
								actionUrl = new URL(action);
							} catch(MalformedURLException e){
								actionUrl = null;
							}
							
							// check for null and if https is not used in action field of form
							if(actionUrl != null && !actionUrl.getProtocol().equals("https")) {
								LOGGER.debug("Found bad form");
								return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
								
							} else if(actionUrl == null && !url.getProtocol().equals("https")) {	// actionURL is null --> relative path or action field is empty
								LOGGER.debug("Found bad form");
								return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
							}
						}
					} else {
						LOGGER.warn("Extraction of input tags from form tag returned null");
					}
				}
				
				// no bad forms found
				LOGGER.debug("Did not find any bad forms");
				return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing form tags");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
