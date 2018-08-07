package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class SuspiciousUrlTaskTest {
	@Test
    public void testSuspiciousUrlTaskHyphenPositive() throws Exception {	
		SuspiciousUrlTask task = new SuspiciousUrlTask(new URL("http://www.test-domain.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousUrlTaskAtPositive() throws Exception {	
		SuspiciousUrlTask task = new SuspiciousUrlTask(new URL("http://www.test.com@http://www.phishing.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousUrlTaskTaskNegative() throws Exception {	
		SuspiciousUrlTask task = new SuspiciousUrlTask(new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousUrlTaskNull() throws Exception {	
		SuspiciousUrlTask task = new SuspiciousUrlTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
