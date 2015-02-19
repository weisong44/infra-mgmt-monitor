package com.weisong.infra.monitor.agent;

import com.weisong.infra.monitor.common.MonitoringData;

public interface MonitoringDataFactory {
	MonitoringData createMonitoringData(ModuleReporter reporter);
}
