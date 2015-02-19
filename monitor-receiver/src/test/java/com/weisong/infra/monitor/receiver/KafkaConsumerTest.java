package com.weisong.infra.monitor.receiver;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.weisong.infra.monitor.receiver.KafkaConsumer.Listener;

public class KafkaConsumerTest extends  Thread {
	
	private boolean messageReceived;
	private KafkaConsumer consumer;

	@Before
	public void init() {
		consumer = new KafkaConsumer("weisong-log-kafka-1", 2181, "logs", "big-consumer-group");
	}
	
	@Test
	public void testKafkaConsumer() throws Exception {
		consumer.start(new Listener() {
			@Override public void onMessage(String message) {
				messageReceived = true;
				System.out.println(message);
			}
		});
		
		Thread.sleep(1000);
		Assert.assertTrue("No message received", messageReceived);
	}
	
	@After
	public void cleanup() {
		if(consumer != null) {
			consumer.shutdown();
		}
	}
}