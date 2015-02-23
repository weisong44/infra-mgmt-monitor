package com.weisong.infra.monitor.receiver;

import com.weisong.infra.monitor.common.MonitoringData;

public interface LogWriter {
	void write(MonitoringData data) throws Exception;
}
