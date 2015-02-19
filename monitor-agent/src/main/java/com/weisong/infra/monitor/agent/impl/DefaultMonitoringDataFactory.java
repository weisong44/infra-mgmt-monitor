package com.weisong.infra.monitor.agent.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.weisong.infra.monitor.agent.ModuleReporter;
import com.weisong.infra.monitor.agent.MonitoringDataFactory;
import com.weisong.infra.monitor.agent.util.AddrUtil;
import com.weisong.infra.monitor.common.MonitoringData;

public class DefaultMonitoringDataFactory implements MonitoringDataFactory {
	
	final static private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss Z");
	
	private String hostname;
	private String ipAddr;
	
	public DefaultMonitoringDataFactory() {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "unknown";
		}
		ipAddr = AddrUtil.getHostIpAddress();
	}
	
	public MonitoringData createMonitoringData(ModuleReporter reporter) {
		MonitoringData data = new MonitoringData(reporter.getName());
		data.setPath(reporter.getPath());
		data.setHostname(hostname);
		data.setIpAddr(ipAddr);
		data.setTimestamp(df.format(new Date()));
		return data;
	}

}
