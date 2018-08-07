package com.da.antiphish.tasks;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.da.antiphish.lists.ListsHandler;

public class BlacklistTaskTest {
	private static ListsHandler listsHandler;
	
	@BeforeClass
	public static void setUp() throws Exception {
		listsHandler = new ListsHandler();
	}
	
	@Test
    public void testBlacklistTaskPositive() throws Exception {	
        BlacklistTask task = new BlacklistTask(new URL(listsHandler.getBlacklist().entrySet().iterator().next().getValue().getUrl()), listsHandler.getBlacklist());
        TaskResult result = task.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testBlacklistTaskNegative() throws Exception {	
        BlacklistTask task = new BlacklistTask(new URL("http://localhost"), listsHandler.getBlacklist());
        TaskResult result = task.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testBlacklistTaskNull() throws Exception {	
        BlacklistTask task = new BlacklistTask(null, null);
        TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
