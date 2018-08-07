package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class DotsTaskTest {
	@Test
    public void testDotsTaskWithWWWPositive() throws Exception {	
        DotsTask task = new DotsTask(new URL("http://www.test.test.test.test.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDotsTaskWithoutWWWPositive() throws Exception {	
        DotsTask task = new DotsTask(new URL("http://test.test.test.test.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDotsTaskWithWWWNegative() throws Exception {	
        DotsTask task = new DotsTask(new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDotsTaskWithoutWWWNegative() throws Exception {	
        DotsTask task = new DotsTask(new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testBlacklistTaskNull() throws Exception {	
		DotsTask task = new DotsTask(null);
		TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
