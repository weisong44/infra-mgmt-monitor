package com.weisong.infra.monitor.agent;

import org.springframework.jmx.export.annotation.ManagedAttribute;

import com.weisong.infra.monitor.common.MonitoringData;

public interface ModuleReporter {

	static public interface Populator {
		void populate(MonitoringData data);
	}
	
    @ManagedAttribute String getName();
    @ManagedAttribute String getPath();
	MonitoringData createReport();
}
