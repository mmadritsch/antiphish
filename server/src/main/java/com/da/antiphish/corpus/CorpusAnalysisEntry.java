package com.da.antiphish.corpus;

import java.util.HashMap;
import java.util.Map;

/**
 * CorpusAnalysisEntry represents an entry of an analysed website and includes
 * the following information:
 * <ul>
 * <li>url: the URL of the analysed website</li>
 * <li>phishing: indicator if the analysed website is a phishing or a legitimate site</li>
 * <li>taskResults: map of all task results of the analysis</li>
 * </ul>
 * 
 * @author Marco Madritsch
 */
public class CorpusAnalysisEntry {
	private String url;
	private boolean phishing;
	private Map<String, Map<String, Double>> taskResults;
	
	/**
	* Constructor with no arguments instantiates the taskResults hashmap.
	*/
	public CorpusAnalysisEntry() { 
		this.taskResults = new HashMap<String, Map<String, Double>>();
	}
	
	/**
	* Constructor with all possible arguments.
	* @param url 			the URL of the analysed website
	* @param phishing		indicator if the analysed website is a phishing or a legitimate site
	* @param taskResults	map of all task results of the analysis
	*/
	public CorpusAnalysisEntry(String url, Map<String, Map<String, Double>> taskResults, boolean phishing) {
		this.url = url;
		this.taskResults = taskResults;
		this.phishing = phishing;
	}
	
	/**
	* Gets the url of the entry.
	* @return the url of the entry
	*/
	public String getUrl() {
		return this.url;
	}
	
	/**
	* Sets the url of the entry.
	* @param url	the new url of the entry
	*/
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	* Gets the phishing indicatro of the entry.
	* @return the phishing indicator of the entry
	*/
	public boolean getPhishing() {
		return this.phishing;
	}
	
	/**
	* Sets the phishing indicator of the entry.
	* @param phishing	the new phishing indicator of the entry
	*/
	public void setPhishing(boolean phishing) {
		this.phishing = phishing;
	}
	
	/**
	* Gets the map of all task results of the entry.
	* @return the map of all task results of the entry
	*/
	public Map<String, Map<String, Double>> getTaskResults() {
		return this.taskResults;
	}
	
	/**
	* Sets the map of all task results of the entry.
	* @param taskResults	the new map of all task results of the entry
	*/
	public void setTaskResults(Map<String, Map<String, Double>> taskResults) {
		this.taskResults = taskResults;
	}
}
