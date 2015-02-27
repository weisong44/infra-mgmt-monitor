package com.weisong.infra.monitor.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

final public class MonitoringData {
	
	static public class Event {
		@Getter private String type; 
	}

	@Getter @Setter private String ipAddr; 
	@Getter @Setter private String hostname; 
	@Getter @Setter private String timestamp;
	@Getter @Setter private String path; 

	@Getter private String name; 
	
	private Map<String, Object> properties;
	private Map<String, Number> counters;
	private List<Event> events;

	protected MonitoringData() {
	}
	
	public MonitoringData(String name) {
		this.name = name;
	}
	
	public Map<String, Object> getProperties() {
		if(properties == null) {
			properties = new HashMap<String, Object>();
		}
		return properties;
	}
	
	public Map<String, Number> getCounters() {
		if(counters == null) {
			counters = new HashMap<String, Number>();
		}
		return counters;
	}
	
	public List<Event> getEvents() {
		if(events == null) {
			events = new LinkedList<Event>();
		}
		return events;
	}
	
	public void addProperty(String key, Object value) {
		getProperties().put(key, value);
	}
	
	public void addCounter(String name, Number value) {
		getCounters().put(name, value);
	}
	
	public void addEvent(Event event) {
		getEvents().add(event);
	}
}
