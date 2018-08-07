package com.da.antiphish.lists;

/**
 * ContentlistEntry represents an entry of the phishing content list and includes the following information:
 * <ul>
 * <li>id: the id of the entry</li>
 * <li>text: the text (keyword or phrase)</li>
 * <li>lang: the language of the text</li>
 * <li>confidence: the confidence of the text</li>
 * </ul>
 * 
 * @author Marco Madritsch
 * 
 */
public class ContentlistEntry {
	private long id;
	private String text;
	private String lang;
	private double confidence;
	
	/**
	* Constructor with all possible arguments.
	* @param id 		the id of the entry
	* @param text		the text (keyword or phrase)
	* @param lang		the language of the text
	* @param confidence	the confidence of the text
	*/
	public ContentlistEntry(long id, String text, String lang, double confidence) {
		this.id = id;
		this.text = text;
		this.lang = lang;
		this.confidence = confidence;
	}
	
	/**
	* Gets the id of the entry.
	* @return the id of the entry
	*/
	public long getId() {
		return this.id;
	}
	
	/**
	* Sets the id of the entry.
	* @param id	the new id of the entry
	*/
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	* Gets the text of the entry.
	* @return the text of the entry
	*/
	public String getText() {
		return this.text;
	}
	
	/**
	* Gets the language of the text.
	* @return the language of the text
	*/
	public String getLang() {
		return this.lang;
	}
	
	/**
	* Gets the confidence of the text.
	* @return the confidence of the text
	*/
	public double getConfidence() {
		return this.confidence;
	}
}
