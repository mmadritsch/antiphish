package com.da.antiphish.tasks;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class MetaTagsTaskTest {
	private static ListsHandler listsHandler;
	private static Document htmlDocMetaWithKeywords;
	private static Document htmlDocMetaWithoutKeywords;
	private static Document htmlDocWithoutMeta;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
		
		htmlDocMetaWithKeywords = Jsoup.parse("<html><head>"
				+ "<meta name=\"keywords\" content=\"referrals,password\">"
				+ "</head><body></body></html>");
		
		htmlDocMetaWithoutKeywords = Jsoup.parse("<html><head>"
				+ "<meta name=\"keywords\" content=\"example,metadata,keywords\">"
				+ "</head><body></body></html>");
		
		htmlDocWithoutMeta = Jsoup.parse("<html><head></head><body></body></html>");
	}
	
	@Test
    public void testMetaTagsTaskPositive() throws Exception {	
		MetaTagsTask task = new MetaTagsTask(htmlDocMetaWithKeywords, listsHandler.getMetalist());
        TaskResult result = task.call();

        assertTrue(result.getScore() > 0.0 && result.getScore() <= 1.0);
    }
	
	@Test
    public void testMetaTagsTaskNegative() throws Exception {	
		MetaTagsTask task = new MetaTagsTask(htmlDocMetaWithoutKeywords, listsHandler.getMetalist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testMetaTagsTaskHtmlDocWithoutMeta() throws Exception {	
		MetaTagsTask task = new MetaTagsTask(htmlDocWithoutMeta, listsHandler.getMetalist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testMetaTagsTaskNull() throws Exception {	
		MetaTagsTask task = new MetaTagsTask(null, null);
		TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
