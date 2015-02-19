package com.weisong.infra.monitor.receiver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.junit.Test;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

public class JRobinTest {
	
	final static private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss Z");
	
	boolean shutdown = false;
	
	@Test
	public void testRead() throws Exception {
		
		BufferedReader br = new BufferedReader(new FileReader(
				"/tmp/monitor/monitor.log"));
		while(!shutdown) {
			String line = br.readLine();
			if(line == null) {
				Thread.sleep(1000);
				continue;
			}
			
			MonitoringData data = JsonUtil.toObject(line, MonitoringData.class);
			if("report".equals(data.getName()) == false) {
				continue;
			}
			
			long timestamp = df.parse(data.getTimestamp()).getTime() / 1000;
			String str = String.format("%d:%s:%s", timestamp, 
				data.getCounters().get("test-value"), 
				data.getCounters().get("memory-heap-used")
			);
			
			System.out.println(String.format("%s  %s", data.getTimestamp(), str));
			
			RrdDb db = null;
			try {
				String dbName = String.format("/tmp/monitor/%s.%s.%s.rrd",
						data.getIpAddr().replace(".", "-"), data.getPath(), data.getName());
				db = openDb(dbName, timestamp);
				try {
					db.createSample().setAndUpdate(str);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
			finally {
				db.close();
			}
		}
	}
	
	private RrdDb openDb(String dbName, long ts) throws Exception {
		RrdDb db = null;
		try {
			db = new RrdDb(dbName);
		} 
		catch (IOException e) {
			RrdDef rrdDef = new RrdDef(dbName);
			rrdDef.setStartTime(ts - 1);
			rrdDef.setStep(5);
			rrdDef.addDatasource("test-value", "GAUGE", 120, Double.NaN, Double.NaN);
			rrdDef.addDatasource("memory-heap-used", "GAUGE", 120, Double.NaN, Double.NaN);
			rrdDef.addArchive("LAST", 0.5, 1, 120);
			rrdDef.addArchive("AVERAGE", 0.5, 10, 12);
			db = new RrdDb(rrdDef);
		}
		return db;
	}
}
