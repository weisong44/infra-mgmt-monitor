package com.weisong.infra.monitor.agent;

import com.weisong.infra.monitor.common.MonitoringData;

public interface ModuleReporter {

	static public interface Populator {
		void populate(MonitoringData data);
	}
	
	String getName();
	String getPath();
	
	MonitoringData createReport();
	MonitoringData createMonitoringData(String name);
	void sendMonitoringData(String name, Populator populator);
}
