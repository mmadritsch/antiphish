package com.da.antiphish.tasks;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class SuspiciousScriptContentTaskTest {
	private static ListsHandler listsHandler;
	private static Document htmlDocPositiv;
	private static Document htmlDocNegativ;
	private static Document htmlDocWithoutScripts;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
		
		htmlDocPositiv = Jsoup.parse("<html><head>"
				+ "<script type=\"text/javascript\">"
				+ "document.getElementById('snpwderr').classname=\"test\";"
				+ "document.getElementById(\"test\").innerHTML = \"This is an example\";"
				+ "</script>"
				+ "</head><body></body></html>");
		
		htmlDocNegativ = Jsoup.parse("<html><head></head><body>"
				+ "<script type=\"text/javascript\">"
				+ "document.getElementById(\"test\").innerHTML = \"This is an example\";"
				+ "</script>"
				+ "</body></html>");
		
		htmlDocWithoutScripts = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testSuspiciousScriptContentTaskPositive() throws Exception {	
		SuspiciousScriptContentTask task = new SuspiciousScriptContentTask(htmlDocPositiv, listsHandler.getScriptlist());
        TaskResult result = task.call();

        assertTrue(result.getScore() > 0.0 && result.getScore() <= 1.0);
    }
	
	@Test
    public void testSuspiciousScriptContentTaskNegative() throws Exception {	
		SuspiciousScriptContentTask task = new SuspiciousScriptContentTask(htmlDocNegativ, listsHandler.getScriptlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousScriptContentTaskHtmlDocWithoutScripts() throws Exception {	
		SuspiciousScriptContentTask task = new SuspiciousScriptContentTask(htmlDocWithoutScripts, listsHandler.getScriptlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testSuspiciousScriptContentTaskNull() throws Exception {	
		SuspiciousScriptContentTask task = new SuspiciousScriptContentTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
