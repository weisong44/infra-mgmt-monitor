package com.weisong.infra.monitor.agent;

import java.rmi.registry.Registry;

import javax.management.remote.JMXConnectorServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

import com.weisong.common.javaconfig.CommonCombinedJavaConfig;
import com.weisong.infra.monitor.agent.impl.DefaultMonitoringAgent;
import com.weisong.infra.monitor.agent.impl.DefaultMonitoringDataFactory;
import com.weisong.infra.monitor.agent.reporter.MainModuleReporter;

@Configuration
@Import({
    CommonCombinedJavaConfig.class
})
public class MonitoringJavaConfig {
    
    final static String JMX_RMI_URL = "service:jmx:rmi://localhost/jndi/rmi://localhost:%d/jmxrmi";
    
    @Value("${rmi.port:1099}") private int rmiPort;
    
    @Autowired private RmiRegistryFactoryBean rmiRegistryFactoryBean;
    @Autowired private ConnectorServerFactoryBean connectorServerFactoryBean;
    
    @Bean
    public RmiRegistryFactoryBean rmiRegistryFactoryBean() {
        RmiRegistryFactoryBean factoryBean = new RmiRegistryFactoryBean();
        factoryBean.setAlwaysCreate(true);
        factoryBean.setPort(rmiPort);
        return factoryBean;
    }
    
    @Bean 
    public Registry rmiRegistry() throws Exception {
        return rmiRegistryFactoryBean.getObject();
    }
    
    @Bean
    public ConnectorServerFactoryBean connectorServerFactoryBean() throws Exception {
        ConnectorServerFactoryBean factoryBean = new ConnectorServerFactoryBean();
        factoryBean.setObjectName("weisong.monitor:type=server,name=ConnectorServerFactoryBean");
        factoryBean.setServiceUrl(String.format(JMX_RMI_URL, rmiPort));
        return factoryBean;
    }
    
    @Bean
    public JMXConnectorServer jmxConnectorServer() throws Exception {
        return connectorServerFactoryBean.getObject();
    }
    
    @Bean
    public MonitoringAgent getMonitoringAgent() {
    	return new DefaultMonitoringAgent();
    }

    @Bean
    public MonitoringDataFactory getMonitoringFactory() {
    	return new DefaultMonitoringDataFactory();
    }
    
    @Bean
    public MainModuleReporter getMainReporter() {
    	return new MainModuleReporter();
    }
}
