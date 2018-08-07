package com.da.antiphish.corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CorpusHelper is a helper class which contains only static method in order to read a website or a corpus analysis
 * out of a JSON file.
 * 
 * @author Marco Madritsch
 *
 */
public class CorpusHelper {
	private final static Logger LOGGER = LoggerFactory.getLogger(HtmlHelper.class);
	
	private CorpusHelper() {}
	
	/**
	 * Reads the given json file (website) and stores it in a JsonNode object.
	 * @param file	the json file including the website and the url of it
	 * @return		the website as JsonNode object
	 */
	public static JsonNode readWebsiteFromFile(File file) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = null;
		
		try {
			node = objectMapper.readTree(file);

		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while reading json from file " + file.getName());
		}
		
		return node;
	}
	
	/**
	* Reads all corpus analysis results out of the given file, stores them in a list of CorpusAnalysisEntry objects
	* and returns that list.
	* @param file	the file with the corpus analysis results
	* @return		a list of CorpusAnalysisEntry objects
	*/
	public static List<CorpusAnalysisEntry> readCorpusAnalysisFromFile(File file) {
		JsonParser jsonParser;
		List<CorpusAnalysisEntry> corpusAnalysisList = new ArrayList<CorpusAnalysisEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(new InputStreamReader(new FileInputStream(file)));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY){
				CorpusAnalysisEntry entry = jsonParser.readValueAs(CorpusAnalysisEntry.class);
				
				if(entry != null) {
					corpusAnalysisList.add(entry);
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn(e.getClass().getSimpleName() + " while reading corpus analysis from file " + file.getName());
			return null;
		}
		
		return corpusAnalysisList;
	}
}
