package com.weisong.infra.monitor.receiver.influxdb;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.weisong.infra.monitor.receiver.KafkaLogReceiver;

public class InfluxDbKafkaLogReceiverMain {
	
	@Configuration
	@Import({
	    KafkaLogReceiver.JavaConfig.class
	})
	public class JavaConfig {
		@Bean 
		InfluxDbLogWriter getInfluxDbWriter() {
			String influxDbHost = System.getProperty("influxdb.host");
			return new InfluxDbLogWriter(influxDbHost);
		}
	}
	
	static void printUsageAndExit() {
		System.out.println("Usage:\n"
			+ "    java " + InfluxDbKafkaLogReceiverMain.class.getName() + " <kafka-host> <influx-db-host>");
		System.exit(-1);
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) {
		if(args.length != 2) {
			printUsageAndExit();
		}
		
		System.setProperty("kafka.host", args[0]);
		System.setProperty("influxdb.host", args[1]);
		
		new AnnotationConfigApplicationContext(JavaConfig.class);
	}

}
