package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class PunctuationTaskTest {
	@Test
    public void testParametersTaskPositive() throws Exception {	
		PunctuationTask task = new PunctuationTask(new URL("http://www.test.com/test!#$%&,;%%%%%%%%%!!!!!!!&&&&&&&&&&$$$$$$$$$$$$'"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testParametersTaskNeagtive() throws Exception {	
		PunctuationTask task = new PunctuationTask(new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testParametersTaskNull() throws Exception {	
		PunctuationTask task = new PunctuationTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
