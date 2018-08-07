package com.da.antiphish.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * FormContentExtractor extracts and counts all suspicious form keywords/contents from all forms of all phishing 
 * websites. Only extracted keywords/contents which's occurrence is higher than 2% of the number of all analysed 
 * phishing sites will be considered, otherwise they are not representative and falsify results. Then the occurrence 
 * of that keywords/contents are counted in all legitimates sites and afterwards the confidence of each keyword/content 
 * will be calculated. All results ares stored in an own JSON file and are necessary for FormTagsTask's formList 
 * list (form_content.json).
 * 
 * @author Marco Madritsch
 */
public class FormContentExtractor {
	private final static Logger LOGGER = LoggerFactory.getLogger(FormContentExtractor.class);
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Calls method to extract and count all form keywords/contents from all forms of all phishing websites (based on a 
	 * training corpus - path in variable trainingPath) and stores them in a map. Calls afterwards method to count the 
	 * occurrence of that extracted keywords/contents on all legitimate websites, calculates for each entry the 
	 * corresponding confidence and saves results in an own JSON file (path in variable resultPath).
	 */
	public static void main(String[] args) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("com.da.antiphish");
		((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
		
		// training corpus
        File trainingPhishingDirectory = new File(trainingPath + "/phishing");
        
        // training legitimate corpus
        File trainingLegitimateDirectory = new File(trainingPath + "/legitimate");
		
        // extract keywords
		Map<String, Double[]> formKeywords = extractFormKeywords(trainingPhishingDirectory.listFiles());
		
		// count extracted keywords in legitimate sites
		formKeywords = countFormKeywordsInLegitimate(trainingLegitimateDirectory.listFiles(), formKeywords);
		
		// calculate confidence for each entry and save it at index 2 of array
		for(Entry<String, Double[]> entry : formKeywords.entrySet()) {
			// confidence = occurencePhishing / (occurencePhishing + occurrenceLegitimate)
			entry.getValue()[2] = entry.getValue()[0]/(entry.getValue()[0] + entry.getValue()[1]);
		}
		
		// sort map based on confidence
		Comparator<Entry<String, Double[]>> valueComparator = (e1, e2) -> e2.getValue()[2].compareTo(e1.getValue()[2]);

		Map<String, Double[]> sortedMap = formKeywords.entrySet().stream().sorted(valueComparator)
		    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/formKeywords.json"), sortedMap);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving keywords to json file");
		}
	}
	
	/**
	 * Iterates over each file (phishing website), extracts and counts all form keywords/contents from plain text and 
	 * element attribute values of all forms (splits contents based on regular expression [\\W_]+). Filters at the end 
	 * all low count keywords/contents (occurrence smaller than 2 % of number of all analysed files/websites) in order 
	 * to get a representative keywords/contents list for all phishing websites, not just for the used corpus.
	 * @param files	the array of all files (websites) to be analysed
	 * @return 		a Map<String, Double[]> where the key is the keyword/content and the array includes the counters
	 * 				index 0: counter for occurrences on phishing websites (counts this method)
	 * 				index 1: counter for occurrences on legitimate websites (counts method countFormKeywordsInLegitimate())
	 */
	public static Map<String, Double[]> extractFormKeywords(File[] files) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		// hashmap for counting all keywords found in all form tags
		Map<String, Double[]> formKeywords = new HashMap<String, Double[]>();
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// form tags of each read website
      		Elements formTags;
      		
	 		// read each website from file and extract all from tags
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
	            
	    		// get all form tags of website
				formTags = HtmlHelper.extractFormTags(htmlDoc);
				
				// check for null
				if(formTags != null) {
					success++;
					
					// iterate over each form tag an extract words from plain text and element attribute values
					for(Element formTag : formTags) {
						// plain text
						String[] words = formTag.text().split("[\\W_]+");
						
						for(int i = 0; i < words.length; i++) {
							if(formKeywords.containsKey(words[i].toLowerCase())) {
								formKeywords.get(words[i].toLowerCase())[0] += 1.0;
							} else {
								formKeywords.put(words[i].toLowerCase(), new Double[]{1.0, 0.0, 0.0});
							}
						}
						
						// all attributes
						List<String> attibuteValues = new ArrayList<String>();
						for(Element e : formTag.getAllElements()){  
							// collect attribute values
						    for(Attribute att : e.attributes().asList()) {
						    	attibuteValues.add(att.getValue());
						    }
						}
						
						// update form keywords list
						for(String word : attibuteValues) {
							if(formKeywords.containsKey(word.toLowerCase())) {
								formKeywords.get(word.toLowerCase())[0] += 1;
							} else {
								formKeywords.put(word.toLowerCase(), new Double[]{1.0, 0.0, 0.0});
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites analysis failed: " + fail);
				}
	        }
	        
	        // filter low count words (occurrence smaller than 2 % of number of all analysed websites)
	        // otherwise they falsify results
	        formKeywords.entrySet().removeIf(e -> e.getValue()[0] < (files.length*0.02));
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return formKeywords;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
	
	/**
	 * Iterates over each file (legitimate website) and counts how often each keyword/content of the key set of the 
	 * map formKeywords occurs in all forms of that websites. Updates the corresponding counter (index 1 of the double 
	 * array in the map formKeywords) of each keyword/content.
	 * @param files			the array of all files (websites) to be analysed
	 * @param formKeywords	the map of all keywords/contents from all phishing websites including their number of
	 * 						occurrences
	 * @return 				the updated Map<String, Double[]> formKeywords including also the number of occurrences of
	 * 						each keyword/content on all legitimate website (stored at index 1 of the double array)
	 */
	public static Map<String, Double[]> countFormKeywordsInLegitimate(File[] files, Map<String, Double[]> formKeywords) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// form tags of each read website
      		Elements formTags;
      		
	 		// read each website from file and count form keywords based on map formKeywords
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
	            
	    		// get all form tags of website
	    		formTags = HtmlHelper.extractFormTags(htmlDoc);
				
	    		// check for null
	    		if(formTags != null) {
					success++;
					
					// iterate over each form tag an extract words from plain text and element attribute values
					for(Element formTag : formTags) {
						// plain text
						String[] words = formTag.text().split("[\\W_]+");
						
						for(int i = 0; i < words.length; i++) {
							if(formKeywords.containsKey(words[i].toLowerCase())) {
								formKeywords.get(words[i].toLowerCase())[1] += 1.0;
							}
						}
						
						// all attributes
						List<String> attibuteValues = new ArrayList<String>();
						for(Element e : formTag.getAllElements()){  
							// collect attribute values
							for(Attribute att : e.attributes().asList()) {
								attibuteValues.add(att.getValue());
							}
						}
						
						// update form keywords list
						for(String word : attibuteValues) {
							if(formKeywords.containsKey(word.toLowerCase())) {
								formKeywords.get(word.toLowerCase())[1] += 1.0;
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites analysis failed: " + fail);
				}  
	        }
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return formKeywords;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
}
