package com.weisong.infra.monitor.agent.reporter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;

import com.weisong.infra.monitor.common.MonitoringData;

final public class MainModuleReporter extends BaseModuleReporter {

    @Value("${rmi.port:1099}") private int rmiPort;
    @Value("${app.name:undefined}") @Setter private String appName;
    
    public MainModuleReporter() {
    	super(null);
    }
    
	@Override
	public String getName() {
		return appName;
	}
	
	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public MonitoringData createReport() {
		MonitoringData report = factory.createMonitoringData(this);
		report.addProperty("jmxPort", rmiPort);

		// Operating system
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean(); 
		report.addCounter("load", os.getSystemLoadAverage());
		
		// Memory
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		report.addCounter("memory-heap-max", mem.getHeapMemoryUsage().getMax());
		report.addCounter("memory-heap-used", mem.getHeapMemoryUsage().getUsed());
		report.addCounter("memory-heap-committed", mem.getHeapMemoryUsage().getCommitted());
		report.addCounter("memory-non-heap-usage-max", mem.getNonHeapMemoryUsage().getMax());
		report.addCounter("memory-non-heap-usage-used", mem.getNonHeapMemoryUsage().getUsed());
		report.addCounter("memory-non-heap-usage-committed", mem.getNonHeapMemoryUsage().getCommitted());
		report.addCounter("memory-pending-finalization", mem.getObjectPendingFinalizationCount());
		Runtime rt = Runtime.getRuntime();
		report.addCounter("memory-total", rt.totalMemory());
		report.addCounter("memory-free", rt.freeMemory());
		report.addCounter("memory-max", rt.maxMemory());
		// Thread
		ThreadMXBean t = ManagementFactory.getThreadMXBean();
		report.addCounter("thread-count", t.getThreadCount());
		report.addCounter("thread-started-count", t.getTotalStartedThreadCount());

		return report;
	}
}
