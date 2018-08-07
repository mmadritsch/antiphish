package com.da.antiphish.tasks;

import java.net.URL;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if the URL of the website contains an IPv4 or IPv6 address or not.
 *  
 * @author Marco Madritsch
 */
public class IPAddressTask extends Task {
	private static final InetAddressValidator ipAddressValidator = InetAddressValidator.getInstance();
	private URL url;
	
	/**
	* Constructor which gets the URL object of a website.
	* @param url	the URL object of the website to be analysed
	*/
	public IPAddressTask(URL url) {
		this.LOGGER = LoggerFactory.getLogger(IPAddressTask.class);
		this.category = Category.URL;
		this.url = url;
	}
	
	/**
	* Checks if the URL of the website contains an IPv4 or IPv6 address or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null) {
			try {
				// get host from url
				String host = url.getHost();
				
				// check if empty
				if(host.equals("")) {
					LOGGER.warn("Extraction of host returned an empty string");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// remove square brackets ([]) in case of an IPv6 address
				host = host.replaceAll("[\\[|\\]]", "");
				
				// check if host is an ip address or not
				if(ipAddressValidator.isValid(host)) {
					LOGGER.debug("Host \"" + host + "\" of URL \"" + url.toString() + "\" is an IP-Address");
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0);
				} else {
					LOGGER.debug("Host \"" + host + "\" of URL \"" + url.toString() + "\" is not an IP-Address");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing URL \"" + url.toString() + "\"");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
