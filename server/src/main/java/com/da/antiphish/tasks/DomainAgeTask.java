package com.da.antiphish.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.LoggerFactory;

import com.da.antiphish.types.Category;

/**
 * This task checks if the domain age of the URL is less than or equal to a specified threshold or not.
 * 
 * @author Marco Madritsch
 */
public class DomainAgeTask extends Task {
	private static int domainAgeThreshold = 12;	// threshold for domain age (default value)
	private final int processWaitTime = 2;	// how many seconds to wait until whois process will be killed
	private URL url;
	private final String[] creationDateKeywords = new String[]{
			"creation date:", "registration date:", "created:", "registered on:",
			"activated:", "registered:"};	// keywords for parsing creation date from whois command
	
	// date parser patterns
	private String[] parserPatterns = new String[] {"yyyy/MM/dd'T'HH:mm:ss'Z'", "yyyy/MM/dd'T'HH:mm:ss", 
			"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd' 'HH:mm:ss", "yyyy.MM.dd' 'HH:mm:ss", 
			"dd-MMM-yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"
			};
	
	/**
	* Constructor which gets an URL.
	* @param url	the URL of the website to be analysed
	*/
	public DomainAgeTask(URL url) {
		this.LOGGER = LoggerFactory.getLogger(DomainAgeTask.class);
		this.category = Category.URL;
		this.url = url;
	}
	
	/**
	* Checks if the domain age of the URL is less than or equal to the specified threshold (domainAgeThreshold) or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(url != null) {
			
			try {
				// extract domain name from url
				String domainName = url.getHost();
				
				// extract "main domain" out of whole domain
				// e.g. www.google.com 	 --> 	"main domain" = google.com
				//		www.amazon.co.uk -->	"main domain" = amazon.co.uk
				if(domainName.startsWith("www.")) {
					// remove www.
					domainName = domainName.substring(4);
				}
				
				List<String> mainDomains = new ArrayList<String>();
				if(domainName != "") {
					String[] domainParts = domainName.split("\\.");
					
					// check number of domains
					if(domainParts.length > 2) {
						// add domain <third-level-domain>.<second-level-domain>.<top-level-domain>
						// and domain <second-level-domain>.<top-level-domain> to list for whois request
						mainDomains.add(domainParts[domainParts.length-3] + "." + domainParts[domainParts.length-2] 
								+ "." + domainParts[domainParts.length-1]);
						mainDomains.add(domainParts[domainParts.length-2] + "." + domainParts[domainParts.length-1]);
					} else if(domainParts.length > 1) {
						// add only domain <second-level-domain>.<top-level-domain> to list for whois request
						mainDomains.add(domainParts[domainParts.length-2] + "." + domainParts[domainParts.length-1]);
					}
					else {
						LOGGER.warn("Domain name of URL " + url.toString() + " is invalid");
				    	return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
					}
					
				} else {
					LOGGER.warn("Domain name of URL " + url.toString() + " is empty");
			    	return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
		        
				// execute whois commands
				int domainAge;
				if(mainDomains.size() > 1) {
					// perform whois command for domain <third-level-domain>.<second-level-domain>.<top-level-domain>
					domainAge = executeWhois(mainDomains.get(0));
					
					// check result
					if(domainAge >= 0 && domainAge < domainAgeThreshold) {
						return new TaskResult(this.getClass().getSimpleName(), category, 1.0, domainAge);
					} else if(domainAge >= 0 && domainAge >= domainAgeThreshold) {
						return new TaskResult(this.getClass().getSimpleName(), category, 0.0, domainAge);
					}
				}
				
				// perform whois command for domain <second-level-domain>.<top-level-domain>
				if(mainDomains.size() > 1) {
					domainAge = executeWhois(mainDomains.get(1));
				} else {
					domainAge = executeWhois(mainDomains.get(0));
				}
				
				// check result
				if(domainAge >= 0 && domainAge <= domainAgeThreshold) {
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0, domainAge);
				} else if(domainAge >= 0 && domainAge > domainAgeThreshold) {
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0, domainAge);
				} else if(domainAge == -2) {	
					// command successful but creation date could not be found
					return new TaskResult(this.getClass().getSimpleName(), category, 1.0, 0.0);
				} else {
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing domain age");
	        	return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}
	
	/**
	* Executes a whois command for the given domain in order to determine the age of the domain in months.
	* @param domain	the domain for the whois command
	* @return 		the age of the domain in months if it could be determined, -1 in case of a failure and -2 if
	* 				creation date could not be determined
	*/
	public int executeWhois(String domain) {
		// process for executing whois command and String and reader for reading result
		String line;
        Process whoisProcess;
        BufferedReader br = null;
        
        try {
	        // execute whois command
	        LOGGER.debug("Execute \"whois " + domain + "\"");
	        whoisProcess = Runtime.getRuntime().exec("whois " + domain);
	        
	        // wait max. processWaitTime seconds, then kill process and return -1
	        if(!whoisProcess.waitFor(processWaitTime, TimeUnit.SECONDS)) {
	            //timeout - kill the process
	        	whoisProcess.destroy();
	            LOGGER.debug("whois command timeout, process killed");
	            
	            return -1;
	        }
	        
	        br = new BufferedReader(new InputStreamReader(whoisProcess.getInputStream()));
	        
	        // get process results
	        while ((line = br.readLine()) != null) {               
	            // parse domain creation date
	            if(Arrays.stream(creationDateKeywords).anyMatch(line.toLowerCase()::contains)) {
	            	LOGGER.debug("Parsed creation date: " + line);
	            	
	            	// parse date
	            	int startIndex = line.indexOf(":") + 1;
	        		int endIndex = line.length();
	        		String stringCreationDate = line.substring(startIndex, endIndex);
	        		stringCreationDate = stringCreationDate.trim();
	        		
	        		// check if string starts with "before" - often at co.uk domains which were registered before 1996
	        		if(stringCreationDate.startsWith("before")) {
	        			stringCreationDate = "01-" + stringCreationDate.substring(7);
	        		}
	        		
	        		Date tmpDate = DateUtils.parseDateStrictly(stringCreationDate, parserPatterns);
	        		LOGGER.debug("Creation date of domain " + domain + ": " + stringCreationDate);
//	            	LocalDate creationDate = LocalDate.parse(stringCreationDate);
	            	LocalDate creationDate = tmpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            	
	            	// calculate domain age in months
	            	LocalDate today = LocalDate.now();
	            	Period period = Period.between(creationDate, today);
	            	int age = period.getYears()*12 + period.getMonths();
	            	
	            	LOGGER.debug(age + " month(s) of max " + 
	            			domainAgeThreshold + " for domain \""  + domain + "\"");
	            	
	            	// return domain age
	            	return age;
	            }
	        }
	        
	        whoisProcess.destroy();
	        
	    } catch (Exception e) {
//	    	e.printStackTrace();
	    	LOGGER.warn(e.getClass().getSimpleName() + " while analysing domain age");
	    	return -1;
	    } finally {
	    	// try to close buffered reader
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
        
        // whois command did not find creation date, return -2 in this case
		return -2;
	}
	
	/**
	* Sets the domain age threshold.
	* @param domainAgeThreshold	the new domain age threshold
	*/
	public static void setDomainAgeThreshold(int domainAgeThreshold) {
		DomainAgeTask.domainAgeThreshold = domainAgeThreshold;
	}
}
