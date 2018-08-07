package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class IPAddressTaskTest {
	@Test
    public void testIPAddressTaskV4Positive() throws Exception {
		IPAddressTask task = new IPAddressTask(new URL("http://176.0.0.1/index.html"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testIPAddressTaskV6Positive() throws Exception {
		IPAddressTask task = new IPAddressTask(new URL("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/index.html"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testIPAddressTaskNegative() throws Exception {
		IPAddressTask task = new IPAddressTask(new URL("http://www.test.com/index.html"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testIPAddressTaskNull() throws Exception {	
		IPAddressTask task = new IPAddressTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
