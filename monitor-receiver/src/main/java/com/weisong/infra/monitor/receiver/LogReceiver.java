package com.weisong.infra.monitor.receiver;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.weisong.infra.monitor.receiver.KafkaConsumer.Listener;

public class LogReceiver {
	
	static void printUsageAndExit() {
		System.out.println("Usage:\n"
			+ "    java LogReceiver <kafka-host> <influx-db-host>");
		System.exit(-1);
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) {
		if(args.length != 2) {
			printUsageAndExit();
		}
		
		String kafkaHost = args[0];
		String influxDbHost = args[1];
		
		new AnnotationConfigApplicationContext(LogReceiverJavaConfig.class);
		
		final InfluxDbWriter dbWriter = new InfluxDbWriter(influxDbHost);
		
		KafkaConsumer consumer = new KafkaConsumer(kafkaHost, 2181, "logs", "log-receivers");
		consumer.start(new Listener() {
			@Override public void onMessage(String message) {
				try {
					dbWriter.write(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
