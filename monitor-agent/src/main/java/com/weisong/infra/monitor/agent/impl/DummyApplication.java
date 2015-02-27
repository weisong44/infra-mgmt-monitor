package com.weisong.infra.monitor.agent.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.weisong.infra.monitor.agent.MonitoringJavaConfig;
import com.weisong.infra.monitor.agent.reporter.BaseModuleReporter;
import com.weisong.infra.monitor.agent.reporter.MainModuleReporter;
import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.common.MonitoringEvent;

public class DummyApplication {
	
	@Configuration
	static public class JavaConfig {
		@Autowired 
		private MainModuleReporter main;
		
		@Bean public WaveReporter waveReporter() {
			return new WaveReporter(main);
		}
	}
	
	@ManagedResource
	static public class WaveReporter extends BaseModuleReporter {

		private double degree;
		
		public WaveReporter(MainModuleReporter main) {
			super(main);
			new Thread() {
				@Override public void run() {
					while(true) {
						MonitoringData data = createMonitoringData();
						data.addEvent("DummyAppEvent")
							.addProperty("time", new Date());
						agent.sendMonitoringData(data);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();;
		}
		
		@Override
		public String getName() {
			return "wave";
		}

		@Override
		public MonitoringData createReport() {
			degree = (degree + 1) % 360.0;
			double value = 2.0 * Math.PI * degree / 360;
			return createMonitoringData()
				.addCounter("sine", Math.sin(value))
				.addCounter("cosine", Math.cos(value));
		}
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) {
		new AnnotationConfigApplicationContext(MonitoringJavaConfig.class, JavaConfig.class);
	}
}
