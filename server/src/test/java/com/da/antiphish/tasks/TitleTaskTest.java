package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class TitleTaskTest {
	private static Document htmlDocWithTitle;
	private static Document htmlDocWithoutTitle;
	
	@BeforeClass
	public static void setUp() throws Exception {
		htmlDocWithTitle = Jsoup.parse("<html><head>"
				+ "<title>Test_Title</title>"
				+ "</head><body></body></html>");
		
		htmlDocWithoutTitle = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testTitleTaskPositive() throws Exception {	
		TitleTask task = new TitleTask(htmlDocWithTitle, new URL("http://www.example.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testTitleTaskNegative() throws Exception {	
		TitleTask task = new TitleTask(htmlDocWithTitle, new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testTitleTaskHtmlDocWithoutTitle() throws Exception {	
		TitleTask task = new TitleTask(htmlDocWithoutTitle, new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testTitleTaskNull() throws Exception {	
		TitleTask task = new TitleTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
