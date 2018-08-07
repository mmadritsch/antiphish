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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.lists.MetalistEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * MetaContentExtractor extracts and counts all suspicious meta tags keywords/contents from
 * all meta tags of all phishing websites. Only extracted keywords/contents which's occurrence is higher than 1 % of
 * the number of all analysed phishing sites will be considered, otherwise they are not representative
 * and falsify results. Then the occurrence of that keywords/contents are counted in all legitimates sites
 * and afterwards the confidence of each keyword/content will be calculated. All keywords/contents with a
 * confidence >= 0.7 will be stored in an own JSON file as result.
 * They are necessary for MetaTagsTask (meta_content.json).
 * 
 * @author Marco Madritsch
 */
public class MetaContentExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetaContentExtractor.class);
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Calls method to extract and count all meta tags keywords/contents from all meta tags of all phishing websites
	 * (based on a training corpus - path in variable trainingPath) and stores them in a map. Calls afterwards method 
	 * to count the occurrence of that extracted keywords/contents on all legitimate websites, calculates for each 
	 * entry the corresponding confidence and saves all keywords/contents with a confidence >= 0.7 in an own JSON file 
	 * (path in variable resultPath).
	 */
	public static void main(String[] args) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("com.da.antiphish");
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// training phishing corpus
        File trainingPhishingDirectory = new File(trainingPath + "/phishing");
        
        // training legitimate corpus
        File trainingLegitimateDirectory = new File(trainingPath + "/legitimate");
		
        // extract content from phishing sites
		Map<String, Integer[]> metaContents = extractMetaContentFromPhishs(trainingPhishingDirectory.listFiles());
		
		// count extracted content in legitimate sites
		metaContents = countMetaContentInLegitimate(trainingLegitimateDirectory.listFiles(), metaContents);
		
		// list for storing contents
		List<MetalistEntry> metaList = new ArrayList<MetalistEntry>();
		
		// calculate confidence of each content
		double confidence;
		for(Entry<String, Integer[]> contentEntry : metaContents.entrySet()) {
			confidence = (double)contentEntry.getValue()[0]/(contentEntry.getValue()[0] + contentEntry.getValue()[1]);
			
			// add only contents with confidence >= 0.7 to list
			if(confidence >= 0.7) {
				metaList.add(new MetalistEntry(0, contentEntry.getKey(), confidence));
			}
		}
		
		// sort list based on confidences
		metaList.sort(Comparator.comparing(MetalistEntry::getConfidence).reversed());
		
		// update id according to sorted version of list
		int id = 0;
		for(MetalistEntry entry : metaList) {
			entry.setId(id);
			id++;
		}
		
		// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/metaContents.json"), metaList);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving meta contents to json file");
		}
	}
	
	/**
	 * Iterates over each file (phishing website), extracts and counts all meta tags keywords/contents from all meta 
	 * tags (splits contents based on regular expression [\\W_]+). Filters at the end all low count keywords/contents 
	 * (occurrence smaller than 1 % of number of all analysed files/websites) in order to get a representative 
	 * keywords/contents list for all phishing websites, not just for the used corpus.
	 * @param files	the array of all files (websites) to be analysed
	 * @return 		a Map<String, Integer[]> where the key is the keyword/content and the array includes the counters
	 * 				index 0: counter for occurrences on phishing websites (counts this method)
	 * 				index 1: counter for occurrences on legitimate websites (counts method 
	 * 				countMetaContentInLegitimate())
	 */
	public static Map<String, Integer[]> extractMetaContentFromPhishs(File[] files) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		// hashmap for counting all contents found in whole meta tags
		Map<String, Integer[]> metaContents = new HashMap<String, Integer[]>();
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// meta tags of each read website
      		Elements metaTags;
      		
	 		// read each website from file, extract meta contents and count it
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
	            
	    		// get all meta tags of website
	    		metaTags = HtmlHelper.extractMetaTags(htmlDoc);
				
	    		// check for null
				if(metaTags != null) {
					success++;
					
					// iterate over each meta tag an extract contents
					for(Element metaTag : metaTags) {
					
						// split meta tags content
						String[] words = metaTag.toString().split("[\\W_]+");
						
						for(int i = 0; i < words.length; i++) {
							if(metaContents.containsKey(words[i].toLowerCase())) {
								metaContents.get(words[i].toLowerCase())[0] += 1;
							} else {
								metaContents.put(words[i].toLowerCase(), new Integer[]{1, 0});
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites analysis failed: " + fail);
				}  
	        }
	        
	        // filter low count contents (occurrence smaller than 1 % of number of all analysed websites)
	        // otherwise they falsify results
	        metaContents.entrySet().removeIf(e -> e.getValue()[0] < (int)(files.length*0.01));
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return metaContents;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
	
	/**
	 * Iterates over each file (legitimate website) and counts how often each keyword/content of the key set of the 
	 * map metaContents occurs in all meta tags of that websites. Updates the corresponding counter (index 1 of the 
	 * integer array in the map metaContents) of each keyword/content.
	 * @param files			the array of all files (websites) to be analysed
	 * @param metaContents	the map of all keywords/contents from all phishing websites including their number of
	 * 						occurrences
	 * @return 				the updated Map<String, Integer[]> metaContents including also the number of occurrences of
	 * 						each keyword/content on all legitimate website (stored at index 1 of the integer array)
	 */
	public static Map<String, Integer[]> countMetaContentInLegitimate(File[] files, Map<String, Integer[]> metaContents) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// meta tags of each read website
      		Elements metaTags;
      		
	 		// read each website from file and count meta contents based on map metaContents
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
	            
	    		// get all meta tags of website
	    		metaTags = HtmlHelper.extractMetaTags(htmlDoc);
				
	    		// check for null
				if(metaTags != null) {
					success++;
					
					// iterate over each meta tag an extract contents
					for(Element metaTag : metaTags) {
					
						// split meta tags content
						String[] words = metaTag.toString().split("[\\W_]+");
						
						for(int i = 0; i < words.length; i++) {
							if(metaContents.containsKey(words[i].toLowerCase())) {
								metaContents.get(words[i].toLowerCase())[1] += 1;
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites analysis failed: " + fail);
				}  
	        }
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return metaContents;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
}
