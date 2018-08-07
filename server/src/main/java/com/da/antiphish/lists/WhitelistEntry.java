package com.da.antiphish.lists;

/**
 * WhitelistEntry represents an entry of the whitelist and includes the following information:
 * <ul>
 * <li>id: the id of the entry</li>
 * <li>url: the url of the legitimate website</li>
 * </ul>
 * 
 * @author MarcoM
 *
 */
public class WhitelistEntry {
	private long id;
	private String url;
	
	/**
	* Constructor with all possible arguments.
	* @param id 	the id of the entry
	* @param url	the url of the legitimate website
	*/
	public WhitelistEntry(long id, String url) {
		this.id = id;
		this.url = url;
	}
	
	/**
	* Gets the id of the entry.
	* @return the id of the entry
	*/
	public long getId() {
		return this.id;
	}
	
	/**
	* Gets the url of the entry.
	* @return the url of the entry
	*/
	public String getUrl() {
		return this.url;
	}
}
