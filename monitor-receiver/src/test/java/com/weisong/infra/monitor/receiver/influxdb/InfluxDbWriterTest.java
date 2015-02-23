package com.weisong.infra.monitor.receiver.influxdb;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.influxdb.InfluxDbLogWriter;
import com.weisong.infra.monitor.util.JsonUtil;

public class InfluxDbWriterTest {
	
	final static public String DB_URL = "http://weisong-log-monitor-1:8086";
	final static public String DB_NAME = "test-metrics";
	
	private boolean shutdown;
	
	@Test
	@SuppressWarnings("resource")
	public void testInfluxDbWriter() throws Exception {
		InfluxDbLogWriter writer = new InfluxDbLogWriter(DB_URL);
		BufferedReader br = new BufferedReader(new FileReader("/tmp/testApp.log"));
		while(!shutdown) {
			String line = br.readLine();
			if(line == null) {
				Thread.sleep(1000);
				continue;
			}
			MonitoringData data = JsonUtil.toObject(line, MonitoringData.class);
			writer.write(data);
		}
	}
}
