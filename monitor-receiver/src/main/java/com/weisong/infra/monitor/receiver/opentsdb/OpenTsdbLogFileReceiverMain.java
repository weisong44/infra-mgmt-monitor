package com.weisong.infra.monitor.receiver.opentsdb;

import java.io.BufferedReader;
import java.io.FileReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.weisong.common.javaconfig.CommonCombinedJavaConfig;
import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.LogWriter;
import com.weisong.infra.monitor.util.JsonUtil;

public class OpenTsdbLogFileReceiverMain {

	@Configuration
	@Import({ CommonCombinedJavaConfig.class })
	static public class JavaConfig {
		@Bean
		OpenTsdbLogWriter getOpenTsdbLogWriter() {
			String dbHost = System.getProperty("opentsdb.host");
			int dbPort = Integer.valueOf(System.getProperty("opentsdb.port"));
			OpenTsdbLogWriter writer = new OpenTsdbLogWriter(dbHost, dbPort);
			writer.start();
			return writer;
		}
	}

	static void printUsageAndExit() {
		System.out.println("Usage:\n"
			+ "    java " + OpenTsdbLogFileReceiverMain.class.getName() + " <opentsdb-host> <log-file>\n");
		System.exit(-1);
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) throws Exception {
		if (args.length != 2) {
			printUsageAndExit();
		}

		String logFileName = args[1];
		
		System.setProperty("opentsdb.host", args[0]);
		System.setProperty("opentsdb.port", "4242");

		ApplicationContext ctx = new AnnotationConfigApplicationContext(JavaConfig.class);
		LogWriter writer = ctx.getBean(LogWriter.class);

		BufferedReader reader = new BufferedReader(new FileReader(logFileName));
		
		String line;
		while (true) {
			line = reader.readLine();
			if (line == null) {
				// wait until there is more of the file for us to read
				Thread.sleep(1000);
				continue;
			}
			
			MonitoringData data = JsonUtil.toObject(line, MonitoringData.class);
			writer.write(data);
		}
	}
}
