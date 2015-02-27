package com.weisong.infra.monitor.common;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class MonitoringEvent {
	@Getter private String type;
	@Getter private Map<String, Object> properties = new HashMap<>();
	
	protected MonitoringEvent() {
	}
	
	public MonitoringEvent(String type) {
		this.type = type;
	}
	
	public MonitoringEvent addProperty(String name, Object value) {
		properties.put(name, value);
		return this;
	}
}
