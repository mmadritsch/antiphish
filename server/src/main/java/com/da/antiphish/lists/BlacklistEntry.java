package com.da.antiphish.lists;

/**
 * BlacklistEntry represents an entry of the phishing blacklist from PhishTank and includes the following information:
 * <ul>
 * <li>id: the phish_id field from a PhishTank entry</li>
 * <li>url: the url of the phishing website</li>
 * </ul>
 * 
 * @author Marco Madritsch
 *
 */
public class BlacklistEntry {
	private long id;
	private String url;
	
	/**
	* Constructor with all possible arguments.
	* @param id 	the phish_id field from a PhishTank entry
	* @param url	the url of the phishing website
	*/
	public BlacklistEntry(long id, String url) {
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
