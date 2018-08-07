package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class FormTagsTaskTest {
	private static ListsHandler listsHandler;
	private static Document htmlDocFormInsecureActionEmpty;
	private static Document htmlDocFormInsecureActionHttp;
	private static Document htmlDocFormInsecureActionImg;
	private static Document htmlDocFormSecureAction;
	private static Document htmlDocFormWithoutKeywords;
	private static Document htmlDocWithoutForm;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
		
		htmlDocFormInsecureActionEmpty = Jsoup.parse("<html><head></head><body>"
				+ "<form action=\"\">"
				+ "<input type=\"text\" name=\"password\">"
				+ "</form>"
				+ "</body></html>");
		
		htmlDocFormInsecureActionHttp = Jsoup.parse("<html><head></head><body>"
				+ "<form action=\"http://www.test.com\">"
				+ "<input type=\"text\" name=\"password\">"
				+ "</form>"
				+ "</body></html>");
		
		htmlDocFormInsecureActionImg = Jsoup.parse("<html><head></head><body>"
				+ "<form action=\"http://www.test.com\">"
				+ "<input type=\"text\" name=\"test\">"
				+ "<img src=\"test.png\">"
				+ "</form>"
				+ "</body></html>");
		
		htmlDocFormSecureAction = Jsoup.parse("<html><head></head><body>"
				+ "<form action=\"https://www.test.com\">"
				+ "<input type=\"text\" name=\"password\">"
				+ "<img src=\"test.png\">"
				+ "</form>"
				+ "</body></html>");
		
		htmlDocFormWithoutKeywords = Jsoup.parse("<html><head></head><body>"
				+ "<form action=\"\">"
				+ "<input type=\"text\" name=\"test\">"
				+ "</form>"
				+ "</body></html>");
		
		htmlDocWithoutForm = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testFormTagsTaskInsecureActionEmptyPositive() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionEmpty, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskInsecureActionEmptyNegative() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionEmpty, new URL("https://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskInsecureActionHttpPositive1() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionHttp, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskInsecureActionHttpPostive2() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionHttp, new URL("https://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskInsecureActionImgPositive1() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionImg, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskInsecureActionImgPostive2() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormInsecureActionImg, new URL("https://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskSecureActionHttpNegative() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormSecureAction, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskSecureActionHttpsNegative() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormSecureAction, new URL("https://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskWithoutForm() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocWithoutForm, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskWithoutKeywords() throws Exception {	
		FormTagsTask task = new FormTagsTask(htmlDocFormWithoutKeywords, new URL("http://www.test.com"), listsHandler.getFormlist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testFormTagsTaskNull() throws Exception {	
		FormTagsTask task = new FormTagsTask(null, null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
