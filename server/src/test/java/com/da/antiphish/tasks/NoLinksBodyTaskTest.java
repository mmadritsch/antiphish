package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class NoLinksBodyTaskTest {
	private static Document htmlDocWithLinks;
	private static Document htmlDocWithoutLinks;
	
	@BeforeClass
	public static void setUp() throws Exception {
		htmlDocWithLinks = Jsoup.parse("<html><head></head><body>"
				+ "<input type=\"text\" name=\"password\">"
				+ "<a href=\"http://www.test.com\">Link 1</a>"
				+ "<a href=\"#\">Link 2</a>"
				+ "<a href=\"\">Link 3</a>"
				+ "</body></html>");
		
		htmlDocWithoutLinks = Jsoup.parse("<html><head></head><body><input type=\"text\" name=\"password\"></body></html>");
	}
	
	@Test
    public void testNoLinksBodyTaskPositive() throws Exception {	
		NoLinksBodyTask task = new NoLinksBodyTask(htmlDocWithoutLinks);
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testNoLinksBodyTaskNeagative() throws Exception {
		NoLinksBodyTask task = new NoLinksBodyTask(htmlDocWithLinks);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testNoLinksBodyTaskNull() throws Exception {	
		NoLinksBodyTask task = new NoLinksBodyTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
