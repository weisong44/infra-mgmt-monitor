package com.weisong.infra.monitor.receiver;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

public class InfluxDbWriterTest {
	
	final static public String DB_URL = "http://weisong-log-monitor-1:8086";
	final static public String DB_NAME = "test-metrics";
	
	private boolean shutdown;
	
	@Test
	public void testInfluxDbWriter() throws Exception {
		InfluxDbWriter writer = new InfluxDbWriter(DB_URL);
		BufferedReader br = new BufferedReader(new FileReader(
				"/tmp/testApp.log"));
		while(!shutdown) {
			String line = br.readLine();
			if(line == null) {
				Thread.sleep(1000);
				continue;
			}
			writer.write(line);
		}
	}
}
