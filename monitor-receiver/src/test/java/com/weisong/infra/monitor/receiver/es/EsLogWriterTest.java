package com.weisong.infra.monitor.receiver.es;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

public class EsLogWriterTest {
	
	private EsLogWriter writer;
	
	@Before
	public void setup() {
		writer = new EsLogWriter(EsConfig.clusterName, EsConfig.hostAndPorts);
	}
	
	@After
	public void cleanup() {
		writer.stop();
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
	}
}
