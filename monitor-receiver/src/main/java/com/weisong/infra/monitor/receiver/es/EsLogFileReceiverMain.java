package com.weisong.infra.monitor.receiver.es;

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

public class EsLogFileReceiverMain {

	@Configuration
	@Import({ CommonCombinedJavaConfig.class })
	static public class JavaConfig {
		
		static private String clusterName;
		static private String[] hostAndPorts;
		
		@Bean
		EsLogWriter esLogWriter() {
			EsLogWriter writer = new EsLogWriter(clusterName, hostAndPorts);
			writer.start();
			return writer;
		}
	}

	static void printUsageAndExit() {
		String mainClass = EsLogFileReceiverMain.class.getName();
		System.out.println("Usage:\n"
			+ "    java " + mainClass + " <log-file> <cluster-name> <host:port ...>\n");
		System.exit(-1);
	}

	@SuppressWarnings("resource")
	static public void main(String[] args) throws Exception {
		if (args.length < 3) {
			printUsageAndExit();
		}

		String logFileName = args[0];
		JavaConfig.clusterName = args[1];
		JavaConfig.hostAndPorts = new String[args.length - 2];
		System.arraycopy(args, 2, JavaConfig.hostAndPorts, 0, args.length - 2);

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
