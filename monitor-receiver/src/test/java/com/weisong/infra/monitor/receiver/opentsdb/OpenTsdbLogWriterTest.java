package com.weisong.infra.monitor.receiver.opentsdb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

public class OpenTsdbLogWriterTest {
	
	private OpenTsdbLogWriter writer;
	
	@Before
	public void setup() {
		writer = new OpenTsdbLogWriter("192.168.59.5", 4242);
	}
	
	@After
	public void cleanup() {
		writer.stop();
	}
	
	@Test
	public void testCamelCaseConversion() {
		Assert.assertEquals("", writer.getMetricName("").toString());
		Assert.assertEquals("memory", writer.getMetricName("memory").toString());
		Assert.assertEquals("memory.used", writer.getMetricName("memoryUsed").toString());
		Assert.assertEquals("memory.used.with.some1#-_?", writer.getMetricName("memoryUsedWithSome1#-_?").toString());
	}
	
	@Test
	public void getGetAppName() {
		Assert.assertEquals("", writer.getAppName(""));
		Assert.assertEquals("", writer.getAppName("/"));
		Assert.assertEquals("appName", writer.getAppName("appName"));
		Assert.assertEquals("appName", writer.getAppName("appName/"));
		Assert.assertEquals("appName", writer.getAppName("appName/1"));
		Assert.assertEquals("appName", writer.getAppName("appName/1/2//3"));
	}
	
	@Test
	public void testSend() throws Exception {
		
		writer.start();
		for(int i = 0; i < 50; i++) {
			if(writer.isConnected()) {
				break;
			}
			Thread.sleep(100);
		}
		
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
        		getClass().getResourceAsStream("/testApp.log")))) {
        	String line = null;
        	while((line = reader.readLine()) != null) {
        		MonitoringData data = JsonUtil.toObject(line, MonitoringData.class);
        		writer.write(data);
        	}
        }
        
        synchronized (this) {
			this.wait();
		}
	}
}
