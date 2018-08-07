package com.da.antiphish.tasks;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class OnlyScriptTaskTest {
	private static Document htmlDocPositiv;
	private static Document htmlDocNegativ;
	private static Document htmlDocWithoutScriptsAndBody;
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		htmlDocPositiv = Jsoup.parse("<html><head>"
				+ "<script type=\"text/javascript\">"
				+ "document.location.href = \"http://supportsetting.xyz/update-info/login.html\";"
				+ "document.getElementById(\"test\").innerHTML = \"This is an example\";"
				+ "</script>"
				+ "</head><body></body></html>");
		
		htmlDocNegativ = Jsoup.parse("<html><head></head><body>"
				+ "<script type=\"text/javascript\">"
				+ "document.getElementById(\"test\").innerHTML = \"This is an example\";"
				+ "</script>"
				+ "</body><p>Test</p></html>");
		
		htmlDocWithoutScriptsAndBody = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testOnlyScriptTaskPositive() throws Exception {	
		OnlyScriptTask task = new OnlyScriptTask(htmlDocPositiv);
        TaskResult result = task.call();

        assertTrue(result.getScore() > 0.0 && result.getScore() <= 1.0);
    }
	
	@Test
    public void testOnlyScriptTaskNegative() throws Exception {	
		OnlyScriptTask task = new OnlyScriptTask(htmlDocNegativ);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testOnlyScriptTaskHtmlDocWithoutScriptsAndBody() throws Exception {	
		OnlyScriptTask task = new OnlyScriptTask(htmlDocWithoutScriptsAndBody);
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testOnlyScriptTaskNull() throws Exception {	
		OnlyScriptTask task = new OnlyScriptTask(null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
