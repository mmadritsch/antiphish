package com.da.antiphish.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * CorpusCollector collects a training corpus of 10.000 phishing and 10.000 legitimate sites and a test corpus of 4.000 
 * phishing and 4.000 legitimate sites. Therefore a list of phishing URLs and a list of legitimate URLs is necessary.
 * For the phishing URLs the current blacklist from PhishTank (https://www.phishtank.com/) must be downloaded in JSON
 * format. For the legitimate URLs see file GoodUrls.txt. Also other sources can be used, but then the source code has
 * to be adapted.
 * 
 * @author Marco Madritsch
 */
public class CorpusCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(CorpusCollector.class);
	private static final String storagePath = "C:/Users/Marco/Documents/Corpus/2017-12-07";
	private static final String blacklistPath = "C:/Users/Marco/Documents/Corpus/2017-12-07/blacklist_2017-12-07.json";
	private static final String legitimatelistPath = "C:/Users/Marco/Documents/Corpus/2017-12-07/GoodUrls.txt";
	
	/**
	 * Triggers first collection of all phishing websites and afterwards collection of all legitimate sites.
	 */
	public static void main(String[] args) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
				
		getPhishingSites();
		getLegitimateSites();
	}
	
	/**
	* Reads a subset of 10.000 phishing URLs randomly from PhishTank blacklist, downloads their content and stores them
	* in JSON files under <storagePath>/training/phishing.
	* Reads another subset of 4.000 phishing URLs also randomly from PhishTank blacklist, downloads their content and 
	* stores them in JSON files under <storagePath>/test/phishing.
	* Both subsets are disjunct.
	*/
	public static void getPhishingSites() {		
		// get phishing urls
		List<String> phishingUrls = readPhishsFromFile(blacklistPath);
		
		// list of all already used urls
		List<String> usedUrls = new ArrayList<String>();
		
		// random numbers generator
		Random rand = new Random();
		
		// download and save content of 10.000 random urls to <storagePath>/training/phishing
		int count = 0;
		String url;
		Document htmlDoc;
		while(count < 10000 && (phishingUrls.size() >= usedUrls.size())) {
			do {
				url = phishingUrls.get(rand.nextInt(phishingUrls.size()));
			} while(usedUrls.contains(url));
			
			htmlDoc = HtmlHelper.getUrlContent(url);
			
			if(htmlDoc != null) {
				// save website
				if(saveUrlContent(htmlDoc, url, storagePath + "/training/phishing/phishing_" + count + ".json")) {
					count++;
					usedUrls.add(url);
					LOGGER.debug("Saved phish from URL " + url);
				} else {
					LOGGER.warn("Could not save phish from URL " + url);
				}
			} else {
				LOGGER.warn("Could not request content of URL " + url);
			}
		}
		
		LOGGER.debug("Saved " + count + " training phishing websites to files.");
		
		// download and save content of 4.000 random urls to <storagePath>/test/phishing
		count = 0;
		while((count < 4000) && (phishingUrls.size() >= usedUrls.size())) {
			do {
				url = phishingUrls.get(rand.nextInt(phishingUrls.size()));
			} while(usedUrls.contains(url));
			
			htmlDoc = HtmlHelper.getUrlContent(url);
			
			if(htmlDoc != null) {
				// save website
				if(saveUrlContent(htmlDoc, url, storagePath + "/test/phishing/phishing_" + count + ".json")) {
					count++;
					usedUrls.add(url);
					LOGGER.debug("Saved phish from URL " + url);
				} else {
					LOGGER.warn("Could not save phish from URL " + url);
				}
			} else {
				LOGGER.warn("Could not request content of URL " + url);
			}
		}
		
		LOGGER.debug("Saved " + count + " test phishing websites to files.");
	}
	
	/**
	* Reads a subset of 10.000 legitimate URLs randomly from GoodURLs list, downloads their content and stores them
	* in JSON files under <storagePath>/training/legitimate.
	* Reads another subset of 4.000 legitimate URLs also randomly from GoodURLs list, downloads their content and 
	* stores them in JSON files under <storagePath>/test/legitimate.
	* Both subsets are disjunct.
	*/
	public static void getLegitimateSites() {
		// get legitimate urls
		List<String> legitimateUrls = readLegitimatesFromFile(legitimatelistPath);
		
		// list of all already used urls
		List<String> usedUrls = new ArrayList<String>();
		
		// random numbers generator
		Random rand = new Random();
		
		// download and save content of 10.000 random urls to <storagePath>/training/legitimate
		int count = 0;
		String url;
		Document htmlDoc;
		while((count < 10000) && (legitimateUrls.size() >= usedUrls.size())) {
			do {
				url = legitimateUrls.get(rand.nextInt(legitimateUrls.size()));
			} while(usedUrls.contains(url));
			
			htmlDoc = HtmlHelper.getUrlContent(url);
			
			if(htmlDoc != null) {
				// save website				
				if(saveUrlContent(htmlDoc, url, storagePath + "/training/legitimate/legitimate_" + count + ".json")) {
					count++;
					usedUrls.add(url);
					LOGGER.debug("Saved legitimate site from URL " + url);
				} else {
					LOGGER.warn("Could not save legitimate site from URL " + url);
				}
			} else {
				LOGGER.warn("Could not request content of URL " + url);
			}
		}
		
		LOGGER.debug("Saved " + count + " training legitimate websites to files.");
		
		// download and save content of 4.000 random urls to <storagePath>/test/legitimate
		count = 0;
		while((count < 4000) && (legitimateUrls.size() >= usedUrls.size())) {
			do {
				url = legitimateUrls.get(rand.nextInt(legitimateUrls.size()));
			} while(usedUrls.contains(url));
			
			htmlDoc = HtmlHelper.getUrlContent(url);
			
			if(htmlDoc != null) {
				// save website				
				if(saveUrlContent(htmlDoc, url, storagePath + "/test/legitimate/legitimate_" + count + ".json")) {
					count++;
					usedUrls.add(url);
					LOGGER.debug("Saved legitimate site from URL " + url);
				} else {
					LOGGER.warn("Could not save legitimate site from URL " + url);
				}
			} else {
				LOGGER.warn("Could not request content of URL " + url);
			}
		}
		
		LOGGER.debug("Saved " + count + " test legitimate websites to files.");
	}
	
	/**
	* Saves the HTML content of the given Jsoup Document as json file in the specified path.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @param url		the URL of the website
	* @param path		the path where it should be stored
	* @return			true if success, false otherwise
	*/
	public static boolean saveUrlContent(Document htmlDoc, String url, String path) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
        try {
      
            ObjectNode node = mapper.createObjectNode();
            node.put("url", url);
            node.put("content", htmlDoc.toString());
            
            mapper.writeValue(new File(path), node);
            
            return true;
        }
        catch (Exception e) {
        	LOGGER.warn(e.getClass().getSimpleName() + " while saving HTML content to file");
        }
		
		return false;
	}
	
	/**
	* Reads all phishing urls from the given path (= json blacklist from phish tank) and returns
	* a list of them.
	* @param path	the path where it should be read
	* @return		a list of phishing urls
	*/
	public static List<String> readPhishsFromFile(String path) {
		JsonParser jsonParser;
		List<String> phishingUrls = new ArrayList<String>();
		
		LOGGER.debug("Start reading phishing urls from " + path);
		
		try {
			jsonParser = new MappingJsonFactory().createParser(new InputStreamReader(new FileInputStream(path)));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY){
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					phishingUrls.add(node.get("url").asText());
				}
			}
		
		} catch (IOException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while reading phishing urls from file.");
		} catch (IllegalStateException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while reading phishing urls from file.");
		}
		
		LOGGER.debug("Finished reading phishing urls from " + path);
		
		return phishingUrls;
	}
	
	/**
	* Reads all legitimate urls from the given path (= text file) and returns
	* a list of them.
	* @param path	the path where it should be read
	* @return		a list of legitimate urls
	*/
	public static List<String> readLegitimatesFromFile(String path) {
		BufferedReader br = null;
		List<String> legitimateUrls = new ArrayList<String>();
		
		LOGGER.debug("Start reading legitimate urls from " + path);
		
		try {
			br =  new BufferedReader(new FileReader(path));
			String line = "";
			while ((line = br.readLine()) != null) {
				legitimateUrls.add(line);
			}
			
			br.close();
		
		} catch (IOException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while reading legitimate urls from file.");
		} catch (IllegalStateException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while reading legitimate urls from file.");
		}
		
		LOGGER.debug("Finished reading legitimate urls from " + path);
		
		return legitimateUrls;
	}
}
