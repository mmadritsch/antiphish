package com.da.antiphish.lists;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ListsHandler is responsible for maintaining all lists/maps.
 * 
 * @author Marco Madritsch
 * 
 */
public class ListsHandler {
	private Map<String, BlacklistEntry> blacklist;
	private Map<String, ContentlistEntry> contentlist;
	private Map<String, WhitelistEntry> whitelist;
	private Map<String, ScriptlistEntry> scriptlist;
	private Map<String, MetalistEntry> metalist;
	private List<String> formlist;
	
	/**
	* Constructor with no arguments instantiates the different lists/maps and triggers reading from files.
	*/
	public ListsHandler() {
		this.blacklist = new HashMap<String, BlacklistEntry>();
		this.whitelist = new HashMap<String, WhitelistEntry>();
		this.contentlist = new HashMap<String, ContentlistEntry>();
		this.scriptlist = new HashMap<String, ScriptlistEntry>();
		this.metalist = new HashMap<String, MetalistEntry>();
		this.formlist = new ArrayList<String>();
		
		readBlacklistFromFile();
		readWhitelistFromFile();
		readContentlistFromFile();
		readScriptlistFromFile();
		readMetalistFromFile();
		readFormlistFromFile();
	}
	
	/**
	* Reads the blacklist from file and stores it in a Map, where the key is the URL of the phishing website and the 
	* value is a BlacklistEntry object.
	*/
	public void readBlacklistFromFile() {
		JsonParser jsonParser;
		Map<String, BlacklistEntry> newBlacklist = new HashMap<String, BlacklistEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/blacklist.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY){
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					newBlacklist.put(node.get("url").asText().toLowerCase(), 
							new BlacklistEntry(node.get("phish_id").asLong(), node.get("url").asText()));
				}
			}
			
			setBlacklist(newBlacklist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Reads the whitelist from file and stores it in a Map, where the key is the URL of the legitimate website and the 
	* value is a WhitelistEntry object.
	*/
	public void readWhitelistFromFile() {
		JsonParser jsonParser;
		Map<String, WhitelistEntry> newWhitelist = new HashMap<String, WhitelistEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/whitelist.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY){
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					newWhitelist.put(node.get("url").asText().toLowerCase(), 
							new WhitelistEntry(node.get("id").asLong(), node.get("url").asText()));
				}
			}
			
			setWhitelist(newWhitelist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Reads the phishing plaintext content list from file and stores it in a Map, where the key is the suspicious 
	* content and the value is a ContentlistEntry object.
	*/
	public void readContentlistFromFile() {
		JsonParser jsonParser;
		Map<String, ContentlistEntry> newContentlist = new HashMap<String, ContentlistEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/plaintext_content.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY){
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					newContentlist.put(node.get("text").asText().toLowerCase(), 
							new ContentlistEntry(node.get("id").asLong(), node.get("text").asText(), 
							node.get("lang").asText(), node.get("confidence").asDouble()));
				}
			}
			
			setContentlist(newContentlist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Reads the phishing script list from file and stores it in a Map, where the key is the suspicious script content 
	* and the value is a ScriptlistEntry object.
	*/
	public void readScriptlistFromFile() {
		JsonParser jsonParser;
		Map<String, ScriptlistEntry> newScriptlist = new HashMap<String, ScriptlistEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/script_content.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY) {
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					newScriptlist.put(node.get("content").asText().toLowerCase(), 
							new ScriptlistEntry(node.get("id").asLong(), node.get("content").asText(), 
							node.get("confidence").asDouble()));
				}
			}
			
			setScriptlist(newScriptlist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Reads the phishing meta list from file and stores it in a Map, where the key is the suspicious meta content and 
	* the value is a MetalistEntry object.
	*/
	public void readMetalistFromFile() {
		JsonParser jsonParser;
		Map<String, MetalistEntry> newMetalist = new HashMap<String, MetalistEntry>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/meta_content.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY) {
				ObjectNode node = jsonParser.readValueAsTree();
				
				if(node != null) {
					newMetalist.put(node.get("content").asText().toLowerCase(), 
							new MetalistEntry(node.get("id").asLong(), node.get("content").asText(), 
							node.get("confidence").asDouble()));
				}
			}
			
			setMetalist(newMetalist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Reads the phishing form list from file and stores it in a List of Strings.
	*/
	public void readFormlistFromFile() {
		JsonParser jsonParser;
		List<String> newFormlist = new ArrayList<String>();
		
		try {
			jsonParser = new MappingJsonFactory().createParser(
					new InputStreamReader(getClass().getResourceAsStream("/form_content.json")));
			
			if(jsonParser.nextToken() != JsonToken.START_ARRAY) {
				  throw new IllegalStateException("Expected an array");
			}
			
			while(jsonParser.nextToken() != JsonToken.END_ARRAY) {
				String entry = jsonParser.getText();
				
				if(entry != null) {
					newFormlist.add(entry);
				}
			}
			
			setFormlist(newFormlist);
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Gets the blacklist.
	* @return the blacklist
	*/
	public Map<String, BlacklistEntry> getBlacklist() {
		return this.blacklist;
	}
	
	/**
	* Sets the blacklist.
	* @param whitelist	the new blacklist
	*/
	public void setBlacklist(Map<String, BlacklistEntry> blacklist) {
		this.blacklist = blacklist;
	}
	
	/**
	* Gets the whitelist.
	* @return the whitelist
	*/
	public Map<String, WhitelistEntry> getWhitelist() {
		return this.whitelist;
	}
	
	/**
	* Sets the whitelist.
	* @param whitelist	the new whitelist
	*/
	public void setWhitelist(Map<String, WhitelistEntry> whitelist) {
		this.whitelist = whitelist;
	}
	
	/**
	* Gets the phishing plaintext content list.
	* @return the phishing plaintext content list
	*/
	public Map<String, ContentlistEntry> getContentlist() {
		return this.contentlist;
	}
	
	/**
	* Sets the phishing plaintext content list.
	* @param contentlist	the new phishing plaintext content list
	*/
	public void setContentlist(Map<String, ContentlistEntry> contentlist) {
		this.contentlist = contentlist;
	}
	
	/**
	* Gets the phishing script list.
	* @return the phishing script list
	*/
	public Map<String, ScriptlistEntry> getScriptlist() {
		return this.scriptlist;
	}
	
	/**
	* Sets the phishing script list.
	* @param scriptlist	the new phishing script list
	*/
	public void setScriptlist(Map<String, ScriptlistEntry> scriptlist) {
		this.scriptlist = scriptlist;
	}
	
	/**
	* Gets the phishing meta list.
	* @return the phishing meta list
	*/
	public Map<String, MetalistEntry> getMetalist() {
		return this.metalist;
	}
	
	/**
	* Sets the phishing meta list.
	* @param metalist	the new phishing meta list
	*/
	public void setMetalist(Map<String, MetalistEntry> metalist) {
		this.metalist = metalist;
	}
	
	/**
	* Gets the phishing form list.
	* @return the phishing form list
	*/
	public List<String> getFormlist() {
		return this.formlist;
	}
	
	/**
	* Sets the phishing form list.
	* @param formlist	the new phishing form list
	*/
	public void setFormlist(List<String> formlist) {
		this.formlist = formlist;
	}
}
