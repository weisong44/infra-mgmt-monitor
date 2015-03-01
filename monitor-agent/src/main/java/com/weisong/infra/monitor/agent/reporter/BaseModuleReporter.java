package com.weisong.infra.monitor.agent.reporter;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.weisong.infra.monitor.agent.ModuleReporter;
import com.weisong.infra.monitor.agent.MonitoringAgent;
import com.weisong.infra.monitor.agent.util.DateUtil;
import com.weisong.infra.monitor.agent.util.HostUtil;
import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.util.JsonUtil;

@ManagedResource
abstract public class BaseModuleReporter implements ModuleReporter {
	
	@Autowired protected MonitoringAgent agent;

	protected ModuleReporter parent;
	
	public BaseModuleReporter(ModuleReporter parent) {
		this.parent = parent;
		if(parent == null && this instanceof MainModuleReporter == false) {
			throw new RuntimeException("Parent can not be null!");
		}
	}

	@PostConstruct
	private void register() {
		agent.register(this);
	}
	
	@Override
	public String getPath() {
		return parent.getPath() + "/" + getName();
	}
	
	@ManagedOperation
	public String createJsonReport() {
		return JsonUtil.toJsonString(createReport());
	}
	
	@ManagedOperation
	public void sendReport() {
		agent.sendMonitoringData(createReport());
	}

	public MonitoringData createMonitoringData() {
		MonitoringData data = new MonitoringData(getType(), getName());
		data.setPath(getPath());
		data.setHostname(HostUtil.getHostname());
		data.setIpAddr(HostUtil.getHostIpAddress());
		data.setTimestamp(DateUtil.format(new Date()));
		return data;
	}
}
