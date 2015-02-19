package com.weisong.infra.monitor.agent.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.Setter;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.weisong.infra.monitor.agent.ModuleReporter;
import com.weisong.infra.monitor.agent.MonitoringAgent;
import com.weisong.infra.monitor.agent.MonitoringDataFactory;
import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

public class DefaultMonitoringAgent implements MonitoringAgent {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Logger monitorLogger = LoggerFactory.getLogger("monitor");
	
	@Autowired private MonitoringDataFactory factory;

	@Setter	@Value("${monitor.agent.reporting.interval:30}")
	private int reportingInterval; // in seconds
	
	@Value("${monitor.agent.log.file:undefined}")
	private String logFileName;
	
	@Value("${app.name}")
	private String appName;
	
	private Set<ModuleReporter> reporters = new HashSet<>();
	
	@PostConstruct
	private void startReportingThread() {
		logger.debug("Adding monitoring log appender");
		addMonitoringLogAppender();
		logger.debug("Starting reporting thread");
		new ReportingThread().start();
	}
	
	@Override
	public void register(ModuleReporter reporter) {
		for(ModuleReporter r : reporters) {
			if(r.getPath().equals(reporter.getPath())) {
				RuntimeException ex = new RuntimeException(String.format(
					"Module reporter with path %s of class %s is already registered", 
					r.getPath(), r.getClass().getName()));
				logger.error("Failed to register report", ex);
				throw ex;
			}
		}
		reporters.add(reporter);
		logger.info(String.format("Registered %s [%s] [total=%d]", 
			reporter.getPath(), reporter.getClass().getSimpleName(), reporters.size()));
	}

	private void doReport() {
		long startTime = System.nanoTime();
		for(ModuleReporter r : reporters) {
			try {
				MonitoringData report = r.createReport();
				sendMonitoringData(report);
			}
			catch (Throwable ex) {
				logger.warn(String.format("Failed to send report for %s", r.getPath()));
			}
		}
		float totalTime = 0.000001f * (System.nanoTime() - startTime);
		logger.debug(String.format("Reporting completed in %.2f ms for %d reporters", 
				totalTime, reporters.size()));
	}

	private String convertToJson(MonitoringData data) {
		String json = JsonUtil.toJsonString(data)
				.replace("\n", "")
				.replace("\r", "");
			while(json.contains("  ")) {
				json = json.replace("  ", " ");
			}
			return json;
	}
	
	@Override
	public void sendMonitoringData(MonitoringData data) {
		if(data != null) {
			monitorLogger.info(convertToJson(data)); 
		}
	}
	
	private void addMonitoringLogAppender() {
		
		if("undefined".equals(logFileName)) {
			logFileName = String.format("/tmp/%s.log", appName);
		}
		
		try {
			RollingFileAppender appender = new RollingFileAppender(
					new PatternLayout("%m%n"), logFileName);
			appender.setMaxFileSize("10240KB");
			appender.setMaxBackupIndex(1);
			org.apache.log4j.Logger mLogger = org.apache.log4j.Logger.getLogger("monitor");
			mLogger.setAdditivity(false);
			mLogger.addAppender(appender);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private class ReportingThread extends Thread {
		public void run() {
			setName("MonitoringAgent");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while(true) {
				try {
					doReport();
					Thread.sleep(1000 * reportingInterval);
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
