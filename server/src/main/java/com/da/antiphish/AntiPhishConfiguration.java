package com.da.antiphish;

import io.dropwizard.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.*;

/**
 * The Configuration class specifies environment-specific parameters which
 * are stored in the file config.yml.
 * 
 * @author Marco Madritsch
 */
public class AntiPhishConfiguration extends Configuration {	    
	@NotEmpty
    private String version;
	
	/**
	* Gets the version of the application.
	* @return the application's version
	*/
    @JsonProperty
    public String getVersion() {
        return version;
    }
    
    /**
	* Sets the version of the application.
	* @param the new application's version
	*/
    @JsonProperty
    public void setVersion(String version) {
        this.version = version;
    }
}
