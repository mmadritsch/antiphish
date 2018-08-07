package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class WhitelistTaskTest {
	private static ListsHandler listsHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
	}
	
	@Test
    public void testWhitelistTaskPositive() throws Exception {	
		WhitelistTask task = new WhitelistTask(new URL(listsHandler.getWhitelist().entrySet().iterator().next().getValue().getUrl()), listsHandler.getWhitelist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testWhitelistTaskNegative() throws Exception {
		WhitelistTask task = new WhitelistTask(new URL("http://localhost"), listsHandler.getWhitelist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testWhitelistTaskNull() throws Exception {	
		WhitelistTask task = new WhitelistTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
