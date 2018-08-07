package com.da.antiphish.resources;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.validator.routines.UrlValidator;
import org.hibernate.validator.constraints.NotEmpty;

import com.da.antiphish.analyse.Computer;
import com.da.antiphish.api.AnalysisResult;

/**
 * The AnalysisResultResource class is the only resource provided by the application and is available under the 
 * URI /analyse. It receives HTTP POST or HTTP GET requests and expects a valid URL in the body or as parameter, which 
 * represents the URL of the website to be analyzed. It then triggers the analysis process and sends the result back 
 * in the form of an AnalysisResult object (which is serialized into a JSON format using Jackson).
 * 
 * @author Marco Madritsch
 */
@Path("/analyse")
@Produces(MediaType.APPLICATION_JSON)
public class AnalysisResultResource {
	private final AtomicLong counter;
	private final Computer computer;
	private final UrlValidator urlValidator;
	
	/**
	* Constructor which gets a Computer object (= controller of an analysis) and initializes a counter and an 
	* UrlValidator object for checking URLs.
	* @param computer	the controller of an analysis
	*/
    public AnalysisResultResource(Computer computer) {
    	this.computer = computer;
        this.counter = new AtomicLong();
        this.urlValidator = new UrlValidator(new String[] {"http", "https"}, UrlValidator.ALLOW_ALL_SCHEMES);
    }
    
    /**
	* Method will be called for HTTP GET requests and expects an URL as parameter. Checks if given URL is valid,
	* triggers analysis process and sends result as AnalysisResult object back.
	* @param url	the url of the website to be analysed
	* @return		an AnalysisResult object including an unique counter, the final score and the associated traffic
	* 				light
	*/
	@GET
    public AnalysisResult analyseGet(
    		@QueryParam("url")
    		@NotEmpty 
    		String url) {
		
		if(validateURL(url)) {
			double finalScore = computer.analyse(url);
			
			return new AnalysisResult(counter.incrementAndGet(), finalScore, computer.getTrafficLight(finalScore).value());
		} else {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid URL").build());
		}
    }
	
	/**
	* Method will be called for HTTP POST requests and expects an URL in the body. Checks if given URL is valid,
	* triggers analysis process and sends result as AnalysisResult object back.
	* @param url	the url of the website to be analysed
	* @return		an AnalysisResult object including an unique counter, the final score and the associated traffic
	* 				light
	*/
	@POST
    public AnalysisResult analyse(
    		@NotEmpty 
    		String url) {
    	
		if(validateURL(url)) {
			double finalScore = computer.analyse(url);
			
			return new AnalysisResult(counter.incrementAndGet(), finalScore, computer.getTrafficLight(finalScore).value());
		} else {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid URL").build());
		}
    }
	
	/**
	* Checks if the given String is a valid URL.
	* @param url	the String to be checked
	* @return		true if it's a valid URL, false otherwise
	*/
	public boolean validateURL(String url) {
		// check if the given String is a valid URL or not
	    if (urlValidator.isValid(url)) {
	       return true;
	    } else {
	       return false;
	    }
	}
}
