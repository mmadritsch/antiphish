package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class FrameTaskTest {
	private static Document htmlDocWithIFrame;
	private static Document htmlDocWithoutFrames;
	
	@BeforeClass
	public static void setUp() throws Exception {
		htmlDocWithIFrame = Jsoup.parse("<html><head></head><body>"
				+ "<iframe src=\"http://www.test.at/test.html\"></iframe>"
				+ "</body></html>");
		
		htmlDocWithoutFrames = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testFrameTaskIFramePositive() throws Exception {	
		FrameTask task = new FrameTask(htmlDocWithIFrame, new URL("http://www.google.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFrameTaskIFrameNegative() throws Exception {
		FrameTask task = new FrameTask(htmlDocWithIFrame, new URL("http://www.test.at"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFrameTaskIFrameSubdomainNegative() throws Exception {
		FrameTask task = new FrameTask(htmlDocWithIFrame, new URL("http://test.test.at"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFrameTaskNegative() throws Exception {	
		FrameTask task = new FrameTask(htmlDocWithoutFrames, new URL("http://www.google.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testtestFrameTaskNull() throws Exception {	
		FrameTask task = new FrameTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
