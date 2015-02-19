package com.weisong.infra.monitor.agent.impl;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.weisong.infra.monitor.agent.MonitoringJavaConfig;

public class DummyApplication {
	@SuppressWarnings("resource")
	static public void main(String[] args) {
		new AnnotationConfigApplicationContext(MonitoringJavaConfig.class);
	}
}
