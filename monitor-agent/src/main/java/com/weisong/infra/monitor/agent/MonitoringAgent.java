package com.weisong.infra.monitor.agent;

import com.weisong.infra.monitor.common.MonitoringData;


public interface MonitoringAgent {
	void setReportingInterval(int interval);
	void register(ModuleReporter reporter);
	void sendMonitoringData(MonitoringData data);
}
