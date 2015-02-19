package com.weisong.infra.monitor.agent;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.weisong.infra.monitor.agent.MonitoringJavaConfig;
import com.weisong.infra.monitor.agent.JmxTest.JavaConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MonitoringJavaConfig.class, JavaConfig.class })
public class JmxTest {

	static public class JavaConfig {
		@Bean
		public JmxTestBean testJmxBean() {
			return new JmxTestBean();
		}
	}
	
    @Autowired private JmxTestBean mbean;
    @Value("${rmi.port:1099}") private int rmiPort;

    @Test
    public void testJmx() throws Exception {
        final ObjectName objectName = new ObjectName(JmxTestBean.OBJECT_NAME);
        final JMXServiceURL jmxUrl = new JMXServiceURL(
        		String.format(MonitoringJavaConfig.JMX_RMI_URL, rmiPort));
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl);
        final MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
        {
            // Test number
            final Integer number = (Integer) mbsc.invoke(objectName, "getNumber", null, null);
            Assert.assertEquals(mbean.getNumber(), number);
        }
        {
            // Test number
            final String name = (String) mbsc.invoke(objectName, "getName", null, null);
            Assert.assertEquals(mbean.getName(), name);
        }
    }
}
