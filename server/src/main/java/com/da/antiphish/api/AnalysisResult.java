package com.da.antiphish.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AnalysisResult represents the final result of an analysis and
 * includes the following information:
 * <ul>
 * <li>id: the unique id of the analysis (REST call)</li>
 * <li>score: the calculated score of the analysis between 0.0 and 1.0 if success, -1.0 otherwise</li>
 * <li>trafficLight: the corresponding traffic light</li>
 * </ul>
 * 
 * @author Marco Madritsch
 *
 */
public class AnalysisResult {
	private long id;
	private double score;
	private int trafficLight;
	
	/**
	* Default constructor.
	*/
	public AnalysisResult() {
        // Jackson deserialization
    }
	
	/**
	* Constructor for instantiation with three arguments.
	* @param id				the id of the analysis result
	* @param score			the score of the analysis result
	* @param trafficLight	the traffic light of the analysis result
	*/
    public AnalysisResult(long id, double score, int trafficLight) {
        this.id = id;
        this.score = score;
        this.trafficLight = trafficLight;
    }
    
    /**
	* Gets the id of the analysis result.
	* @return the id of the analysis result
	*/
    @JsonProperty
    public long getId() {
        return id;
    }
    
    /**
	* Gets the score of the analysis result.
	* @return the score of the analysis result
	*/
    @JsonProperty
    public double getScore() {
        return score;
    }
    
    /**
	* Gets the traffic light of the analysis result.
	* @return the traffic light of the analysis result
	*/
    @JsonProperty
    public int getTrafficLight() {
        return trafficLight;
    }
    
    /**
	* Checks if this object (AnalysisResult) is equal to the passed object.
	* @param obj	the passed object
	* @return true if it's equal, false otherwise
	*/
    @Override
    public boolean equals(Object obj) {
    	if(obj == null || !(obj instanceof AnalysisResult))
            return false;
    	
        AnalysisResult other = (AnalysisResult)obj;
        
        return (this.id == other.id) && (this.score == other.score) && (this.trafficLight == other.trafficLight);
    }
	
}
