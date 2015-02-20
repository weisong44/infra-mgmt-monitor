package com.weisong.infra.monitor.agent.reporter;

import java.lang.management.GarbageCollectorMXBean;
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
		
		// Garbage collection
		for(GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
			String name = bean.getName().replace(" ", "");
			report.addCounter("gcCount" + name, bean.getCollectionCount());
			report.addCounter("gcTime" + name, bean.getCollectionTime());
		}

		// Memory
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		report.addCounter("memoryHeapMax", mem.getHeapMemoryUsage().getMax());
		report.addCounter("memoryHeapUsed", mem.getHeapMemoryUsage().getUsed());
		report.addCounter("memoryHeapCommitted", mem.getHeapMemoryUsage().getCommitted());
		report.addCounter("memoryNonHeapMax", mem.getNonHeapMemoryUsage().getMax());
		report.addCounter("memoryNonHeapUsed", mem.getNonHeapMemoryUsage().getUsed());
		report.addCounter("memoryNonHeapCommitted", mem.getNonHeapMemoryUsage().getCommitted());
		report.addCounter("memoryPendingFinalization", mem.getObjectPendingFinalizationCount());
		Runtime rt = Runtime.getRuntime();
		report.addCounter("memoryTotal", rt.totalMemory());
		report.addCounter("memoryFree", rt.freeMemory());
		report.addCounter("memoryMax", rt.maxMemory());
		// Thread
		ThreadMXBean t = ManagementFactory.getThreadMXBean();
		report.addCounter("threadCount", t.getThreadCount());
		report.addCounter("threadStartedCount", t.getTotalStartedThreadCount());

		return report;
	}
}
