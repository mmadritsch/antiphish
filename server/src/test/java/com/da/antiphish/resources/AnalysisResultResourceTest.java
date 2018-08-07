package com.da.antiphish.resources;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import com.da.antiphish.analyse.Computer;
import com.da.antiphish.api.AnalysisResult;
import com.da.antiphish.types.TrafficLight;
import com.yammer.dropwizard.testing.ResourceTest;

public class AnalysisResultResourceTest extends ResourceTest {
	private final AnalysisResult result = new AnalysisResult(1, 0.0, 1);
	private final Computer computer = Mockito.mock(Computer.class);

	@Override
	protected void setUpResources() throws Exception {
		Mockito.when(computer.analyse(Mockito.anyString())).thenReturn(0.0);
		Mockito.when(computer.getTrafficLight(Mockito.anyDouble())).thenReturn(TrafficLight.GREEN);
		addResource(new AnalysisResultResource(computer));
	}
	
	@Test
    public void testAnalysisResultResourceGet() throws Exception {
        assertEquals(client().resource("/analyse?url=http://www.test.com").get(AnalysisResult.class), result);
    }
	
	@Test
    public void testAnalysisResultResourcePost() throws Exception {		
        assertEquals(client().resource("/analyse").post(AnalysisResult.class, "http://www.test.com"), result);
    }
	
}
