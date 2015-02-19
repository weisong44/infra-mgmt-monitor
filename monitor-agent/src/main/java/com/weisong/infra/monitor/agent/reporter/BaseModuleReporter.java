package com.weisong.infra.monitor.agent.reporter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.weisong.infra.monitor.agent.ModuleReporter;
import com.weisong.infra.monitor.agent.MonitoringAgent;
import com.weisong.infra.monitor.agent.MonitoringDataFactory;
import com.weisong.infra.monitor.common.MonitoringData;

abstract public class BaseModuleReporter implements ModuleReporter {
	
    @Autowired protected MonitoringDataFactory factory;
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
	
	@Override
	public MonitoringData createMonitoringData(String name) {
		return factory.createMonitoringData(this);
	}

	@Override
	public void sendMonitoringData(String name, Populator populator) {
		MonitoringData data = createMonitoringData(name);
		populator.populate(data);
		agent.sendMonitoringData(data);
	}

}
