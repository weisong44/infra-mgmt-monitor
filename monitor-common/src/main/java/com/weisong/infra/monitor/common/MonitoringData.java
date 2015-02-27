package com.weisong.infra.monitor.common;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

final public class MonitoringData {

	@Getter @Setter private String ipAddr; 
	@Getter @Setter private String hostname; 
	@Getter @Setter private String timestamp;
	@Getter @Setter private String path; 

	@Getter private String name; 
	
	@Getter private Map<String, Object> properties = new HashMap<>();
	@Getter private Map<String, Number> counters = new HashMap<>();

	protected MonitoringData() {
	}
	
	public MonitoringData(String name) {
		this.name = name;
	}
	
	public void addProperty(String key, Object value) {
		getProperties().put(key, value);
	}
	
	public void addCounter(String name, Number value) {
		getCounters().put(name, value);
	}
}
