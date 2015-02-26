package com.weisong.infra.monitor.agent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.weisong.infra.monitor.agent.DefaultMonitoringAgentTest.JavaConfig;
import com.weisong.infra.monitor.agent.reporter.BaseModuleReporter;
import com.weisong.infra.monitor.agent.reporter.MainModuleReporter;
import com.weisong.infra.monitor.common.MonitoringData;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MonitoringJavaConfig.class, JavaConfig.class })
public class DefaultMonitoringAgentTest {

	static private boolean reportCreated = false;
	
	static class JavaConfig {
		
		@Autowired MainModuleReporter main;
		
		@Bean
		public TestReporter getTestReporter() {
			return new TestReporter(main);
		}
	}

	static class TestReporter extends BaseModuleReporter {
		
		public TestReporter(ModuleReporter parent) {
			super(parent);
		}
		
		@Override
		public String getName() {
			return "test";
		}
		@Override
		public MonitoringData createReport() {
			MonitoringData report = factory.createMonitoringData(this);
			reportCreated = true;
			return report;
		}
	}
	
    @Autowired private MonitoringAgent agent;
    @Autowired private TestReporter reporter;
    
    @Test
    public void testCreateReport() throws Exception {
    	agent.setReportingInterval(1);
    	Thread.sleep(3000);
    	Assert.assertTrue(reportCreated);
    }
}
