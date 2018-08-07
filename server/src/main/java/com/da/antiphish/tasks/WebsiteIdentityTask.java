package com.da.antiphish.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.types.Category;

/**
 * This task checks if the identity of the website has a relationship to the domain of the URL. The identity is the 
 * most frequently referred domain name and is calculated based on all links (<a> tags) of the website. 
 * 
 * @author Marco Madritsch
 */
public class WebsiteIdentityTask extends Task {
	private Document htmlDoc;
	private URL url;
	
	/**
	* Constructor which gets the Document object and the URL object of a website.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param url		the URL object of the website to be analysed
	*/
	public WebsiteIdentityTask(Document htmlDoc, URL url) {
		this.LOGGER = LoggerFactory.getLogger(WebsiteIdentityTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
		this.url = url;
	}
	
	/**
	* Checks if the identity of the website has a relationship to the domain of the URL or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && url != null) {
			// hash map to store and count each referenced domain name
			Map<String,Integer> referredDomains = new HashMap<String,Integer>();
			 
			try {
				// get all links of website
				Elements linkTags = HtmlHelper.extractLinkTags(htmlDoc);
				
				// check for null
				if(linkTags == null) {
					LOGGER.warn("Extraction of link tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// collect URLs of all links and store number of references in hash map
				for(Element linkTag : linkTags) {
					// extract host name from link
					String domainName = "";
					try {
						domainName = (new URL(linkTag.attr("href"))).getHost();
					} catch (MalformedURLException e) {
						// check if link starts with "/"
						if(linkTag.attr("href").startsWith("/")) {
							// link refers to actual domain name
							domainName = url.getHost();
						}
					}
					
					// update/add key, value pair
					if(domainName != "") {
						// check if domain name is already in hash map and increase count by 1 in this case
						// otherwise add new key, value pair with count 1 to hash map
						if(referredDomains.containsKey(domainName)) {
							referredDomains.put(domainName, referredDomains.get(domainName) + 1);
						} else {
							referredDomains.put(domainName, 1);
						}
					}
				}
				
				// get most frequently referred domain name
				if(referredDomains.size() > 0) {
					String mostFrequentDomain = Collections.max(referredDomains.entrySet(), Map.Entry.comparingByValue()).getKey();
					
					// check if most frequently referred domain name starts with "www." and remove it in this case
					if(mostFrequentDomain.startsWith("www.")) {
						mostFrequentDomain = mostFrequentDomain.substring(4);
					}
					
					// check if actual domain name starts with "www." and remove it in this case
					String actualDomain = url.getHost();
					if(actualDomain.startsWith("www.")) {
						actualDomain = actualDomain.substring(4);
					}
					
					// check if this domain name belongs to the actual domain of the website (whether it is equal or there is a subdomain relationship)
					if(mostFrequentDomain.equals(actualDomain) || mostFrequentDomain.endsWith(actualDomain) || actualDomain.endsWith(mostFrequentDomain)) {
						LOGGER.debug("Most frequently referred domain is \"" + mostFrequentDomain + "\" and belongs to actual domain");
						return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
					} else {
						LOGGER.debug("Most frequently referred domain is \"" + mostFrequentDomain + "\" and  does not belong to actual domain");
						return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
					}
					
				} else {
					LOGGER.debug("No website identity found");
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
