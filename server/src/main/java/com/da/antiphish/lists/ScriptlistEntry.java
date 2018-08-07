package com.da.antiphish.lists;

/**
 * ScriptlistEntry represents an entry of the suspicious scripting content list and includes the following information:
 * <ul>
 * <li>id: the id of the entry</li>
 * <li>content: the suspicious content (command)</li>
 * <li>confidence: the confidence of the content</li>
 * </ul>
 * 
 * @author Marco Madritsch
 * 
 */
public class ScriptlistEntry {
	private long id;
	private String content;
	private double confidence;
	
	/**
	* Constructor with all possible arguments.
	* @param id 		the id of the entry
	* @param content	the suspicious content (command)
	* @param confidence	the confidence of the content
	*/
	public ScriptlistEntry(long id, String content, double confidence) {
		this.id = id;
		this.content = content;
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
	* Gets the suspicious content of the entry.
	* @return the suspicious content of the entry
	*/
	public String getContent() {
		return this.content;
	}
	
	/**
	* Gets the confidence of the content.
	* @return the confidence of the content
	*/
	public double getConfidence() {
		return this.confidence;
	}
}
