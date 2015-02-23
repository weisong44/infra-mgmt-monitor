package com.weisong.infra.monitor.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.weisong.common.javaconfig.CommonCombinedJavaConfig;
import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.KafkaConsumer.Listener;
import com.weisong.infra.monitor.util.JsonUtil;

public class KafkaLogReceiver {

	@Configuration
	@Import({
	    CommonCombinedJavaConfig.class
	})
	public class JavaConfig {

		@Autowired private LogWriter writer;
		
		@Bean 
		KafkaLogReceiver getKafkaLogReceiver() {
			String kafkaHost = System.getProperty("kafka.host");
			KafkaLogReceiver receiver = new KafkaLogReceiver(kafkaHost, writer);
			receiver.start();
			return receiver;
		}
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String kafkaHost;
	private LogWriter writer;
	
	public KafkaLogReceiver(String kafkaHost, LogWriter writer) {
		this.kafkaHost = kafkaHost;
	}

	public void start() {
		KafkaConsumer consumer = new KafkaConsumer(kafkaHost, 2181, "logs", "log-receivers");
		consumer.start(new Listener() {
			@Override public void onMessage(String logMessage) {
				MonitoringData data = JsonUtil.toObject(logMessage, MonitoringData.class);
				if(data != null) {
					try {
						writer.write(data);
					}
					catch (Throwable e) {
						logger.warn(String.format("Failed to write %s, %s", 
							data.getPath(), e.getMessage()));
					}
				}
				else {
					logger.info("Failed to write message: " + logMessage);
				}
			}
		});
	}
	
}
