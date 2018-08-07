package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.lists.ContentlistEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * PlaintextKeywordsExtractor extracts and counts all suspicious plaintext keywords from the whole plaintext of all 
 * phishing websites. Only extracted keywords which's occurrence is higher than 1 % of the number of all analysed 
 * phishing sites will be considered, otherwise they are not representative and falsify results. Then the occurrence 
 * of that keywords are counted in all legitimates sites and afterwards the confidence of each keyword will be 
 * calculated. All keywords with a confidence >= 0.7 will be stored in an own JSON file as result. They are necessary 
 * for PlaintextContentTask (plaintext_content.json).
 * 
 * @author Marco Madritsch
 */
public class PlaintextContentExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlaintextContentExtractor.class);
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Calls method to extract and count all plaintext keywords from the whole plaintext of all phishing websites
	 * (based on a training corpus - path in variable trainingPath) and stores them in a map. Calls afterwards method 
	 * to count the occurrence of that extracted keywords on all legitimate websites, calculates for each entry the 
	 * corresponding confidence and saves all keywords with a confidence >= 0.7 in an own JSON file (path in variable 
	 * resultPath).
	 */
	public static void main(String[] args) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("com.da.antiphish");
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// training phishing corpus
        File trainingPhishingDirectory = new File(trainingPath + "/phishing");
        
        // training legitimate corpus
        File trainingLegitimateDirectory = new File(trainingPath + "/legitimate");
		
        // extract keywords from phishing sites
		Map<String, Integer[]> plaintextKeywords = 
				extractPlaintextKeywordsFromPhishs(trainingPhishingDirectory.listFiles());
		
		// count extracted keywords in legitimate sites
		plaintextKeywords = countPlaintextKeywordsInLegitimate(trainingLegitimateDirectory.listFiles(), 
				plaintextKeywords);
		
		// list for storing keyword
		List<ContentlistEntry> contentList = new ArrayList<ContentlistEntry>();
		
		// calculate confidence of each keyword
		double confidence;
		for(Entry<String, Integer[]> keywordEntry : plaintextKeywords.entrySet()) {
			confidence = (double)keywordEntry.getValue()[0]/(keywordEntry.getValue()[0] + keywordEntry.getValue()[1]);
			
			// add only keywords with confidence >= 0.7 to list
			if(confidence >= 0.7) {
				contentList.add(new ContentlistEntry(0, keywordEntry.getKey(), "en", confidence));
			}
		}
		
		// sort list based on confidences
		contentList.sort(Comparator.comparing(ContentlistEntry::getConfidence).reversed());
		
		// update id according to sorted version of list
		int id = 0;
		for(ContentlistEntry entry : contentList) {
			entry.setId(id);
			id++;
		}
		
		// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/plaintextKeywords.json"), contentList);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving keywords to json file");
		}
	}
	
	/**
	 * Iterates over each file (phishing website), extracts and counts all plaintext keywords from the whole plaintext 
	 * (splits plaintext based on regular expression [\\W_]+). Filters at the end all low count keywords (occurrence 
	 * smaller than 1 % of number of all analysed files/websites) in order to get a representative keywords list for 
	 * all phishing websites, not just for the used corpus.
	 * @param files	the array of all files (websites) to be analysed
	 * @return 		a Map<String, Integer[]> where the key is the keyword and the array includes the counters
	 * 				index 0: counter for occurrences on phishing websites (counts this method)
	 * 				index 1: counter for occurrences on legitimate websites (counts method 
	 * 				countPlaintextKeywordsInLegitimate())
	 */
	public static Map<String, Integer[]> extractPlaintextKeywordsFromPhishs(File[] files) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		// hashmap for counting all keywords found in whole plaintext
		Map<String, Integer[]> plaintextKeywords = new HashMap<String, Integer[]>();
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// string for extracted plaintext
      		String plaintext;
      		
	 		// read each website from file, extract plaintext keywords and count it
	        for(File file : files) {
	        	total++;
	        	
	        	// read website from file
	        	websiteNode = CorpusHelper.readWebsiteFromFile(file);
	        	
	        	// get html content
	    		htmlDoc = Jsoup.parse(websiteNode.path("content").asText());
	    		
	    		// check if content is null
	    		if(htmlDoc == null) {
	    			continue;
	    		}
	            
	    		// get whole plaintext of website
				plaintext = HtmlHelper.extractPlaintext(htmlDoc);
				
				// check for emptiness
				if(!plaintext.equals("")) {
					success++;
					
					// split plain text
					String[] words = plaintext.split("[\\W_]+");
					
					// save words in map
					for(int i = 0; i < words.length; i++) {
						// check if it is a word (only letters)
						if(words[i].matches("[a-zA-Z]+")) {
							if(plaintextKeywords.containsKey(words[i].toLowerCase())) {
								plaintextKeywords.get(words[i].toLowerCase())[0] += 1;
							} else {
								plaintextKeywords.put(words[i].toLowerCase(), new Integer[]{1, 0});
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites without plaintext: " + fail);
				}  
	        }
	        
	        // filter low count words (occurrence smaller than 1 % of number of all analysed websites)
	        // otherwise they falsify results
	        plaintextKeywords.entrySet().removeIf(e -> e.getValue()[0] < (files.length*0.01));
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " with plaintext, " + fail + " without plaintext");
	        
	        return plaintextKeywords;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
	
	/**
	 * Iterates over each file (legitimate website) and counts how often each keyword of the key set of the map 
	 * plaintextKeywords occurs in the plaintext of that websites. Updates the corresponding counter (index 1 of the 
	 * integer array in the map plaintextKeywords) of each keyword.
	 * @param files				the array of all files (websites) to be analysed
	 * @param plaintextKeywords	the map of all keywords from all phishing websites including their number of occurrences
	 * @return 					the updated Map<String, Integer[]> plaintextKeywords including also the number of 
	 * 							occurrences of each keyword/content on all legitimate website (stored at index 1 of the 
	 * 							integer array)
	 */
	public static Map<String, Integer[]> countPlaintextKeywordsInLegitimate(File[] files, Map<String, Integer[]> plaintextKeywords) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// string for extracted plaintext
      		String plaintext;
      		
	 		// read each website from file and count plaintext keywords based on map plaintextKeywords
	        for(File file : files) {
	        	total++;
	        	
	        	// read website from file
	        	websiteNode = CorpusHelper.readWebsiteFromFile(file);
	        	
	        	// get html content
	    		htmlDoc = Jsoup.parse(websiteNode.path("content").asText());
	    		
	    		// check if content is null
	    		if(htmlDoc == null) {
	    			continue;
	    		}
	            
	    		// get whole plaintext of website
				plaintext = HtmlHelper.extractPlaintext(htmlDoc);
				
				// check for emptiness
				if(!plaintext.equals("")) {
					success++;
					
					// split plain text
					String[] words = plaintext.split("[\\W_]+");
					
					// count words
					for(int i = 0; i < words.length; i++) {
						if(plaintextKeywords.containsKey(words[i].toLowerCase())) {
							plaintextKeywords.get(words[i].toLowerCase())[1] += 1;
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites without plaintext: " + fail);
				}  
	        }
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " with plaintext, " + fail + " without plaintext");
	        
	        return plaintextKeywords;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
}
