package com.weisong.infra.monitor.receiver.influxdb;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.LogWriter;

public class InfluxDbLogWriter implements LogWriter {
	
	final static public String DB_NAME = "weisong-metrics";
	final static public SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z");
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private InfluxDB influxDb;
	
	public InfluxDbLogWriter(String dbHost) {
		String dbUrl = String.format("http://%s:8086", dbHost);
		influxDb = InfluxDBFactory.connect(dbUrl, "weisong", "songwei");
	}
	
	public void createDatabase() {
		influxDb.createDatabase(DB_NAME);
	}
	
	@Override
	public void write(MonitoringData data) throws Exception {
		
		String serieName = String.format("domain/%s/%s", data.getIpAddr(), data.getPath());

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
				
		influxDb.write(DB_NAME, TimeUnit.MILLISECONDS, serie);
		
		logger.info(String.format("%s %s", data.getTimestamp(), serieName));
	}
}
