package com.da.antiphish.configuration;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.BoundType;

/**
 * ConfigurationHandler is responsible for reading all the necessary settings of
 * the server out of the file config.json and for storing it in a Configuration object.
 * 
 * @author Marco Madritsch
 */
public class ConfigurationHandler {
	private Configuration configuration;
	
	/**
	* Constructor with no arguments instantiates a new Configuration object and calls method
	* to read out configuration settings from file config.json.
	*/
	public ConfigurationHandler() {
		this.configuration = new Configuration();
		readConfigurationFromFile();
	}
	
	/**
	* Reads out configuration settings from file config.json.
	*/
	public void readConfigurationFromFile() {
		// jackson object mapper with guava module for deserialisation of ranges
		GuavaModule mod = new GuavaModule().defaultBoundType(BoundType.CLOSED);
		ObjectMapper objectMapper = new ObjectMapper().registerModule(mod);
		
		try {
			setConfiguration(objectMapper.readValue(new File("src/main/resources/config.json"), Configuration.class));

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	* Gets the configuration.
	* @return the configuration
	*/
	public Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	* Sets the configuration.
	* @param configuration	the configuration
	*/
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
