package com.weisong.infra.monitor.agent;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = JmxTestBean.OBJECT_NAME)
public class JmxTestBean {

    final static public String OBJECT_NAME = "weisong.monitor:type=test,name=TestMBean";
	
    @ManagedAttribute
    public Integer getNumber() {
        return 100;
    }
    
    @ManagedAttribute
    public String getName() {
        return "name";
    }
}
