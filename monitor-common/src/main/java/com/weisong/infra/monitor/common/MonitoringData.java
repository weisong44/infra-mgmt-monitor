package com.weisong.infra.monitor.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

final public class MonitoringData {
	
	@Getter @Setter private String ipAddr; 
	@Getter @Setter private String hostname;
	@Getter @Setter private String timestamp;
	@Getter @Setter private String path; 

	@Getter private String name;
	
	@Getter private Map<String, Object> properties = new HashMap<>(5);
	@Getter private Map<String, Number> counters = new HashMap<>(5);
	@Getter private List<MonitoringEvent> events = new LinkedList<>();

	protected MonitoringData() {
	}

	public MonitoringData(String name) {
		this.name = name;
	}

	public MonitoringData addProperty(String key, Object value) {
		properties.put(key, value);
		return this;
	}

	public MonitoringData addCounter(String name, Number value) {
		counters.put(name, value);
		return this;
	}

	public MonitoringEvent addEvent(String type) {
		MonitoringEvent event = new MonitoringEvent(type);
		events.add(event);
		return event;
	}
}
