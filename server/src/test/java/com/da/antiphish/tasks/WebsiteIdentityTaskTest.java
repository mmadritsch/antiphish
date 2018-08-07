package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebsiteIdentityTaskTest {
	private static Document htmlDocWithLinks;
	private static Document htmlDocWithoutLinks;
	
	@BeforeClass
	public static void setUp() throws Exception {		
		htmlDocWithLinks = Jsoup.parse("<html><head></head><body>"
				+ "<a href=\"http://www.test.com\">Link 1</a>"
				+ "<a href=\"http://www.test.com/index.html\">Link 2</a>"
				+ "<a href=\"/index.html\">Link 3</a>"
				+ "</body></html>");
		
		htmlDocWithoutLinks = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testWebsiteIdentityTaskPositive() throws Exception {	
		WebsiteIdentityTask task = new WebsiteIdentityTask(htmlDocWithLinks, new URL("http://www.example.com"));
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testWebsiteIdentityTaskNegative() throws Exception {	
		WebsiteIdentityTask task = new WebsiteIdentityTask(htmlDocWithLinks, new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testWebsiteIdentityTaskHtmlDocWithoutLinks() throws Exception {	
		WebsiteIdentityTask task = new WebsiteIdentityTask(htmlDocWithoutLinks, new URL("http://www.test.com"));
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testWebsiteIdentityTaskNull() throws Exception {	
		WebsiteIdentityTask task = new WebsiteIdentityTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
