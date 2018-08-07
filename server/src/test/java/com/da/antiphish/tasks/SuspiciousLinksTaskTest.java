package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class SuspiciousLinksTaskTest {
	private static Document htmlDocPositiv;
	private static Document htmlDocNegativ;
	private static Document htmlDocWithoutLinks;
	
	@BeforeClass
	public static void setUp() throws Exception {
		htmlDocPositiv = Jsoup.parse("<html><head></head><body>"
				+ "<a href=\"http://www.test-domain.com\">Link 1</a>"
				+ "<a href=\"http://www.test.com@http://www.phishing.com\">Link 2</a>"
				+ "<a href=\"\">Link 3</a>"
				+ "</body></html>");
		
		htmlDocNegativ = Jsoup.parse("<html><head></head><body>"
				+ "<a href=\"#\">Link 1</a>"
				+ "<a href=\"http://www.test.com\">Link 2</a>"
				+ "<a href=\"http://www.test.com\">Link 3</a>"
				+ "</body></html>");
		
		htmlDocWithoutLinks = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testSuspiciousLinksTaskPositive() throws Exception {	
		SuspiciousLinksTask task = new SuspiciousLinksTask(htmlDocPositiv);
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousLinksTaskNegative() throws Exception {	
		SuspiciousLinksTask task = new SuspiciousLinksTask(htmlDocNegativ);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousLinksTaskHtmlDocWithoutLinks() throws Exception {	
		SuspiciousLinksTask task = new SuspiciousLinksTask(htmlDocWithoutLinks);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousLinksTaskNull() throws Exception {	
		SuspiciousLinksTask task = new SuspiciousLinksTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
