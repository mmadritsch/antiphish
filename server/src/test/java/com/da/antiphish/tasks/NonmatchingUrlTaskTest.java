package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class NonmatchingUrlTaskTest {
	private static Document htmlDocNonmatchingLinks;
	private static Document htmlDocMatchingLinks;
	private static Document htmlDocWithoutLinks;
	
	@BeforeClass
	public static void setUp() throws Exception {
		htmlDocNonmatchingLinks = Jsoup.parse("<html><head></head><body>"
				+ "<a href=\"http://www.test.com\">Link 1</a>"
				+ "<a href=\"http://www.test.com\">http://www.example.com</a>"
				+ "<a href=\"http://www.test.com\">http://www.test.com</a>"
				+ "</body></html>");
		
		htmlDocMatchingLinks = Jsoup.parse("<html><head></head><body>"
				+ "<a href=\"http://www.test.com\">Link 1</a>"
				+ "<a href=\"http://www.test.com\">http://www.test.com</a>"
				+ "</body></html>");
		
		htmlDocWithoutLinks = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testNonmatchingUrlTaskPositive() throws Exception {	
		NonmatchingUrlTask task = new NonmatchingUrlTask(htmlDocNonmatchingLinks);
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testNonmatchingUrlTaskNegative() throws Exception {
		NonmatchingUrlTask task = new NonmatchingUrlTask(htmlDocMatchingLinks);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testNonmatchingUrlTaskHtmlDocWithoutLinks() throws Exception {	
		NonmatchingUrlTask task = new NonmatchingUrlTask(htmlDocWithoutLinks);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testNonmatchingUrlTaskNull() throws Exception {	
		NonmatchingUrlTask task = new NonmatchingUrlTask(null);
		TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
