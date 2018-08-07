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
import com.da.antiphish.lists.ScriptlistEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * ScriptContentExtractor extracts and counts all suspicious script contents from all script tags of all phishing 
 * websites. Only extracted contents which's occurrence is higher than 1 % of the number of all analysed phishing sites 
 * will be considered, otherwise they are not representative and falsify results. Then the occurrence of that contents 
 * are counted in all legitimates sites and afterwards the confidence of each content will be calculated. All contents 
 * with a confidence >= 0.7 will be stored in an own JSON file as result. 
 * They are necessary for SuspiciousScriptContentTask (script_content.json).
 * 
 * @author Marco Madritsch
 */
public class ScriptContentExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptContentExtractor.class);
	private static final String trainingPath = "C:/Users/Marco/Documents/Corpus/2017-11-29_new/training";
	private static final String resultPath = "src/main/resources/corpus/2017-11-29_new";
	
	/**
	 * Calls method to extract and count all script contents from all script tags of all phishing websites
	 * (based on a training corpus - path in variable trainingPath) and stores them in a map. Calls afterwards method 
	 * to count the occurrence of that extracted contents on all legitimate websites, calculates for each entry the 
	 * corresponding confidence and saves all contents with a confidence >= 0.7 in an own JSON file (path in variable 
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
		
        // extract content from phishing sites
		Map<String, Integer[]> scriptContents = extractScriptContentFromPhishs(trainingPhishingDirectory.listFiles());
		
		// count extracted content in legitimate sites
		scriptContents = countScriptContentInLegitimate(trainingLegitimateDirectory.listFiles(), scriptContents);
		
		// list for storing contents
		List<ScriptlistEntry> scriptList = new ArrayList<ScriptlistEntry>();
		
		// calculate confidence of each content
		double confidence;
		for(Entry<String, Integer[]> contentEntry : scriptContents.entrySet()) {
			confidence = (double)contentEntry.getValue()[0]/(contentEntry.getValue()[0] + contentEntry.getValue()[1]);
			
			// add only contents with confidence >= 0.7 to list
			if(confidence >= 0.7) {
				scriptList.add(new ScriptlistEntry(0, contentEntry.getKey(), confidence));
			}
		}
		
		// sort list based on confidences
		scriptList.sort(Comparator.comparing(ScriptlistEntry::getConfidence).reversed());
		
		// update id according to sorted version of list
		int id = 0;
		for(ScriptlistEntry entry : scriptList) {
			entry.setId(id);
			id++;
		}
		
		// mapper and node to store result as json file
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.enable(SerializationFeature.INDENT_OUTPUT);
    	
		// save results as json file
        try {
			mapper.writeValue(new File(resultPath + "/scriptContents.json"), scriptList);
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while saving script contents to json file");
		}
	}
	
	/**
	 * Iterates over each file (phishing website), extracts and counts all script contents from all script tags 
	 * (splits script contents based on regular expression [=;]+). Filters at the end all low count contents (occurrence 
	 * smaller than 1 % of number of all analysed files/websites) in order to get a representative contents list for 
	 * all phishing websites, not just for the used corpus.
	 * @param files	the array of all files (websites) to be analysed
	 * @return 		a Map<String, Integer[]> where the key is the content and the array includes the counters
	 * 				index 0: counter for occurrences on phishing websites (counts this method)
	 * 				index 1: counter for occurrences on legitimate websites (counts method 
	 * 				countScriptContentInLegitimate())
	 */
	public static Map<String, Integer[]> extractScriptContentFromPhishs(File[] files) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
				
		// hashmap for counting all contents found in whole script tags
		Map<String, Integer[]> scriptContents = new HashMap<String, Integer[]>();
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// script tags of each read website
      		Elements scriptTags;
      		
	 		// read each website from file, extract script contents and count it
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
	            
	    		// get all script tags of website
	    		scriptTags = HtmlHelper.extractScriptTags(htmlDoc);
				
	    		// check for null
				if(scriptTags != null) {
					success++;
					
					// iterate over each script tag an extract contents
					for(Element scriptTag : scriptTags) {
					
						// split script tags content
						String[] commands = scriptTag.html().toString().replaceAll("\\s","").split("[=;]+");
						
						for(int i = 0; i < commands.length; i++) {
							if(scriptContents.containsKey(commands[i].toLowerCase())) {
								scriptContents.get(commands[i].toLowerCase())[0] += 1;
							} else {
								scriptContents.put(commands[i].toLowerCase(), new Integer[]{1, 0});
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
	        scriptContents.entrySet().removeIf(e -> e.getValue()[0] < (int)(files.length*0.01));
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return scriptContents;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
	
	/**
	 * Iterates over each file (legitimate website) and counts how often each script content of the key set of the map 
	 * scriptContents occurs in all script tags of that websites. Updates the corresponding counter (index 1 of the 
	 * integer array in the map scriptContents) of each content.
	 * @param files				the array of all files (websites) to be analysed
	 * @param scriptContents	the map of all contents from all phishing websites including their number of occurrences
	 * @return 					the updated Map<String, Integer[]> scriptContents including also the number of 
	 * 							occurrences of each content on all legitimate website (stored at index 1 of the 
	 * 							integer array)
	 */
	public static Map<String, Integer[]> countScriptContentInLegitimate(File[] files, Map<String, Integer[]> scriptContents) {
		// counts
		int total = 0;
		int success = 0;
		int fail = 0;
		
		try {
			// node for each website which will be read from json
      		JsonNode websiteNode;
      		
      		// document for each read website
      		Document htmlDoc;
      		
      		// script tags of each read website
      		Elements scriptTags;
      		
	 		// read each website from file and count script contents based on map scriptContents
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
	            
	    		// get all script tags of website
	    		scriptTags = HtmlHelper.extractScriptTags(htmlDoc);
				
	    		// check for null
				if(scriptTags != null) {
					success++;
					
					// iterate over each script tag an extract contents
					for(Element scriptTag : scriptTags) {
					
						// split script tags content
						String[] commands = scriptTag.html().toString().replaceAll("\\s","").split("[=;]+");
						
						for(int i = 0; i < commands.length; i++) {
							if(scriptContents.containsKey(commands[i].toLowerCase())) {
								scriptContents.get(commands[i].toLowerCase())[1] += 1;
							}
						}
					}
				} else {
					fail++;
					LOGGER.warn("Websites analysis failed: " + fail);
				}  
	        }
	        
	        LOGGER.info("Analysed " + total + " websites, " + success + " successfully, " + fail + " failed");
	        
	        return scriptContents;
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while analysing files");
		}
		
		return null;
	}
}
