package com.weisong.infra.monitor.receiver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.junit.Before;
import org.junit.Test;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

public class InfluxDbTest {
	
	final static public String DB_URL = "http://weisong-log-monitor-1:8086";
	final static public String DB_NAME = "test-metrics";
	final static public SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss Z");
	
	private boolean shutdown;
	private InfluxDB influxDb;
	
	@Before
	public void setup() {
		influxDb = InfluxDBFactory.connect(DB_URL, "weisong", "songwei");
	}
	
	protected void createDatabase() {
		influxDb.createDatabase(DB_NAME);
	}
	
	@Test
	@SuppressWarnings("resource")
	public void testInfluxDb() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				"/tmp/testApp.log"));
		while(!shutdown) {
			String line = br.readLine();
			if(line == null) {
				Thread.sleep(1000);
				continue;
			}
			
			MonitoringData data = JsonUtil.toObject(line, MonitoringData.class);
			String serieName = String.format("domain/%s/%s/%s",
					data.getIpAddr(), data.getPath(), data.getName());

			long timestamp = df.parse(data.getTimestamp()).getTime();
			
			Map<String, Number> counters = data.getCounters();
			String[] columns = new String[counters.size() + 2];
			Object[] values = new Number[columns.length];

			// Time column
			columns[0] = "sequence_number";
			values[0] = timestamp;
			
			// Seq column
			columns[1] = "time";
			values[1] = timestamp;
			
			int i = 2;
			for(String c : counters.keySet()) {
				columns[i] = c.replace('-', '_');
				values[i] = counters.get(c);
				++i;
			}
			
			Serie serie = new Serie.Builder(serieName)
				.columns(columns)
				.values(values)
				.build();
			
			String str = String.format("%d %s %s", timestamp, 
				data.getCounters().get("thread-count"), 
				data.getCounters().get("memory-heap-max")
			);
			System.out.println(String.format("%s  %s", data.getTimestamp(), str));
			
			influxDb.write(DB_NAME, TimeUnit.MILLISECONDS, serie);
		}
	}
}
