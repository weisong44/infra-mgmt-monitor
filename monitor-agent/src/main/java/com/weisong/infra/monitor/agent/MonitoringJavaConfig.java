package com.weisong.infra.monitor.agent;

import java.rmi.registry.Registry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

import com.weisong.common.javaconfig.CommonCombinedJavaConfig;
import com.weisong.infra.monitor.agent.impl.DefaultMonitoringAgent;
import com.weisong.infra.monitor.agent.reporter.MainModuleReporter;

@Configuration
@Import({
    CommonCombinedJavaConfig.class
})
public class MonitoringJavaConfig {
    
	final static String MBEAN_DOMAIN = "weisong.monitor";
    final static String JMX_RMI_URL = "service:jmx:rmi://localhost/jndi/rmi://localhost:%d/jmxrmi";
    
    @Value("${app.name}") private String appName;
    @Value("${rmi.port:1099}") private int rmiPort;
    
    @Autowired private RmiRegistryFactoryBean rmiRegistryFactory;
    @Autowired private ConnectorServerFactoryBean connectorServerFactory;

    @Bean
    public RmiRegistryFactoryBean rmiRegistryFactoryBean() {
        RmiRegistryFactoryBean factoryBean = new RmiRegistryFactoryBean();
        factoryBean.setAlwaysCreate(true);
        factoryBean.setPort(rmiPort);
        return factoryBean;
    }
    
    @Bean 
    public Registry rmiRegistry() throws Exception {
        return rmiRegistryFactory.getObject();
    }
    
    @Bean
    public ConnectorServerFactoryBean connectorServerFactoryBean() throws Exception {
        ConnectorServerFactoryBean factory = new ConnectorServerFactoryBean();
        factory.setObjectName(createMBeanName(factory));
        factory.setServiceUrl(String.format(JMX_RMI_URL, rmiPort));
        return factory;
    }

    private ObjectName createMBeanName(Object bean) throws MalformedObjectNameException {
		String name = bean.getClass().getSimpleName();
		if(bean instanceof ModuleReporter) {
			name = ((ModuleReporter) bean).getPath();
		}
		String objName = String.format("%s:application=%s,name=%s", MBEAN_DOMAIN, appName, name);
		return new ObjectName(objName);
    }
    
    @Bean
    public ObjectNamingStrategy objectNamingStrategy() {
    	return new ObjectNamingStrategy() {
			@Override
			public ObjectName getObjectName(Object managedBean, String beanKey)
					throws MalformedObjectNameException {
				return createMBeanName(managedBean);
			}
		};
    }
    
    @Bean
    public JMXConnectorServer jmxConnectorServer() throws Exception {
        return connectorServerFactory.getObject();
    }
    
    @Bean
    public MonitoringAgent monitoringAgent() {
    	return new DefaultMonitoringAgent();
    }
    
    @Bean
    public MainModuleReporter mainReporter() throws Exception {
    	return new MainModuleReporter();
    }
}
