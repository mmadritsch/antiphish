package com.da.antiphish.tasks;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class PlaintextContentTaskTest {
	private static ListsHandler listsHandler;
	private static Document htmlDocPositiv;
	private static Document htmlDocNegativ;
	private static Document htmlDocWithoutText;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
		
		htmlDocPositiv = Jsoup.parse("<html><head></head><body>"
				+ "<p>This is a test paragraph which contains suspicious keywords: antiphishing and denied.</p>"
				+ "</body></html>");
		
		htmlDocNegativ = Jsoup.parse("<html><head></head><body>"
				+ "<p>This is a test paragraph which does not contain any suspicious keywords.\"</p>"
				+ "</body></html>");
		
		htmlDocWithoutText = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testPlaintextContentTaskPositive() throws Exception {	
		PlaintextContentTask task = new PlaintextContentTask(htmlDocPositiv, listsHandler.getContentlist());
        TaskResult result = task.call();

        assertTrue(result.getScore() > 0.0 && result.getScore() <= 1.0);
    }
	
	@Test
    public void testPlaintextContentTaskNegative() throws Exception {	
		PlaintextContentTask task = new PlaintextContentTask(htmlDocNegativ, listsHandler.getContentlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testPlaintextContentTaskWithoutText() throws Exception {	
		PlaintextContentTask task = new PlaintextContentTask(htmlDocWithoutText, listsHandler.getContentlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testPlaintextContentTaskNull() throws Exception {	
		PlaintextContentTask task = new PlaintextContentTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
