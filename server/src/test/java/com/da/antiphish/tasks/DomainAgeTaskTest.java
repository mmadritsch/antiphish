package com.da.antiphish.tasks;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;
import org.mockito.Mockito;

public class DomainAgeTaskTest {
	
	@Test
    public void testDomainAgeTaskNegativeWithWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://www.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(100);
        TaskResult result = taskSpy.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskNegativeWithoutWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(100);
        TaskResult result = taskSpy.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskNegativeWithSubdomain() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(100);
        TaskResult result = taskSpy.call();

        assertEquals(0.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskPositiveWithWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://www.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(0);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskPositiveWithoutWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(0);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskPositiveWithSubdomain() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(0);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskFailureWithWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://www.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-1);
        TaskResult result = taskSpy.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskFailureWithoutWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-1);
        TaskResult result = taskSpy.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskFailureWithSubdomain() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-1);
        TaskResult result = taskSpy.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskDateNotFoundWithWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://www.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-2);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskDateNotFoundWithoutWWW() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-2);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskDateNotFoundWithSubdomain() throws Exception {	
        DomainAgeTask task = new DomainAgeTask(new URL("http://test.test.com"));
        DomainAgeTask taskSpy = Mockito.spy(task);
		Mockito.when(taskSpy.executeWhois(Mockito.anyString())).thenReturn(-2);
        TaskResult result = taskSpy.call();

        assertEquals(1.0, result.getScore(), 0.001);
    }
	
	@Test
    public void testDomainAgeTaskNull() throws Exception {	
		DomainAgeTask task = new DomainAgeTask(null);
		TaskResult result = task.call();

        assertEquals(-1.0, result.getScore(), 0.001);
    }
}
