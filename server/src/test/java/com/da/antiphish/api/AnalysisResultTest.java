package com.da.antiphish.api;

import static org.junit.Assert.*;

import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.*;

public class AnalysisResultTest {
	@Test
	public void serializesToJSON() throws Exception {
	    final AnalysisResult result = new AnalysisResult(1, 0.0, 1);
	    
	    assertEquals(asJson(result), jsonFixture("fixtures/analysisResult.json"));
	}
	
	@Test
	public void deserializesFromJSON() throws Exception {
		final AnalysisResult result = new AnalysisResult(1, 0.0, 1);
		assertEquals(fromJson(jsonFixture("fixtures/analysisResult.json"), AnalysisResult.class), result);
	}
}
