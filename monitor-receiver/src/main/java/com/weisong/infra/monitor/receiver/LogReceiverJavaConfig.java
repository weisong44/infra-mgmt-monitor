package com.weisong.infra.monitor.receiver;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.weisong.common.javaconfig.CommonCombinedJavaConfig;

@Configuration
@Import({
    CommonCombinedJavaConfig.class
})
public class LogReceiverJavaConfig {
	// Nothing to do yet!
}
