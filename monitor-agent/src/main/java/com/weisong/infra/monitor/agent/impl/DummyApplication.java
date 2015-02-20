package com.weisong.infra.monitor.agent.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.weisong.infra.monitor.agent.MonitoringJavaConfig;
import com.weisong.infra.monitor.agent.reporter.BaseModuleReporter;
import com.weisong.infra.monitor.agent.reporter.MainModuleReporter;
import com.weisong.infra.monitor.common.MonitoringData;

public class DummyApplication {
	
	@Configuration
	static public class JavaConfig {
		@Autowired 
		private MainModuleReporter main;
		
		@Bean public WaveReporter getWaveReporter() {
			return new WaveReporter(main);
		}
	}
	
	static public class WaveReporter extends BaseModuleReporter {

		private double degree;
		
		public WaveReporter(MainModuleReporter main) {
			super(main);
		}
		
		@Override
		public String getName() {
			return "wave";
		}
		
		@Override
		public MonitoringData createReport() {
			degree = (degree + 1) % 360.0;
			double value = 2.0 * Math.PI * degree / 360;
			MonitoringData report = factory.createMonitoringData(this);
			report.addCounter("sine", Math.sin(value));
			report.addCounter("cosine", Math.cos(value));
			return report;
		}
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) {
		new AnnotationConfigApplicationContext(MonitoringJavaConfig.class, JavaConfig.class);
	}
}
