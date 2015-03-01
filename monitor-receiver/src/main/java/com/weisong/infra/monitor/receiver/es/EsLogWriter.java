package com.weisong.infra.monitor.receiver.es;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.LogWriter;
import com.weisong.infra.monitor.receiver.LogWriterUtil;

public class EsLogWriter implements LogWriter {

	final static public String INDEX_MIT = "mgmt-info";
	final static public String INDEX_METRICS = "metrics";
	
	final static public long MAX_CONN_IDLE_TIME = 60000L;
	final static public long RECONNECT_INTERVAL = 5000L;
	
	final static public SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z");
	final static public SimpleDateFormat dfIso8601;
	
	static {
		dfIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dfIso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private String clusterName;
	private String[] hostAndPorts;
	
	private TransportClient client;

	private long logsWritten;
	private long logsSkipped;
	
	public EsLogWriter(String clusterName, String ... hostAndPorts) {
		this.clusterName = clusterName;
		this.hostAndPorts = hostAndPorts;
		
		String msg = String.format("Using cluster %s, and nodes", clusterName);
		for(String hp : hostAndPorts) {
			msg += " " + hp;
		}
		logger.info(msg);
	}

	public void start() {
		Settings settings = ImmutableSettings.settingsBuilder()
			.put("cluster.name", clusterName)
			.build();
		client = new TransportClient(settings);
		for(String hostAndPort : hostAndPorts) {
			String[] tokens = hostAndPort.split(":");
			String host = tokens[0];
			int port = 9300; 
			if(tokens.length > 1) {
				port = Integer.valueOf(tokens[1]);
			}
			client.addTransportAddress(new InetSocketTransportAddress(host, port));
		}
		
		String msg = "Connected:";
		for(DiscoveryNode n : client.connectedNodes()) {
			msg += " " + n.getAddress();
		}
		logger.info(msg);
		logger.info("Started");
	}
	
	public void stop() {
		if(client != null) {
			client.close();
			client = null;
		}
		logger.info("Stopped");
	}
	
	public boolean isConnected() {
		return client != null;
	}
	
	@Override
	public void write(MonitoringData data) throws Exception {
		
		String appName = LogWriterUtil.getAppName(data.getPath());
		String parentPath = data.getPath().substring(0, data.getPath().lastIndexOf('/'));
		
		// MIT
		XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
		builder
			.field("path", data.getPath())
			.field("parent", parentPath)
			.field("host", data.getHostname())
			.field("address", data.getIpAddr())
			.field("application", appName)
			.field("name", data.getName())
			.field("type", data.getType());
		for(String name : data.getProperties().keySet()) {
			Object value = data.getProperties().get(name);
			builder.field(name, value);
		}
		builder.endObject();

		try {
			client.prepareUpdate(INDEX_MIT, "data", data.getPath())
				.setDocAsUpsert(true)
				.setDoc(builder)
				.execute()
				.actionGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		// Metrics
		Long timestamp = df.parse(data.getTimestamp()).getTime();
		for(String name : data.getCounters().keySet()) {
			String metricName = LogWriterUtil.getMetricName(name).toString();
			Number value = data.getCounters().get(name);
			String id = String.format("%s:%s:%d", data.getPath(), metricName, timestamp);
			String iso8601Timestamp = dfIso8601.format(df.parse(data.getTimestamp()));
			builder = XContentFactory.jsonBuilder()
				.startObject()
					.field("path", data.getPath())
					.field("host", data.getHostname())
					.field("address", data.getIpAddr())
					.field("application", appName)
					.field("module", data.getName())
					.field("timestamp", data.getTimestamp())
					.field("@timestamp", iso8601Timestamp)
					.field("name", metricName)
					.field("value", value)
				.endObject();
			try {
				client.prepareUpdate(INDEX_METRICS, "data", id)
					.setDocAsUpsert(true)
					.setDoc(builder)
					.execute()
					.actionGet();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(client != null) {
			if(++logsWritten % 1000 == 0) {
				logger.info(String.format("Written logs: %d", logsWritten));
			}
		}
		else {
			if(++logsSkipped % 1000 == 0) {
				logger.info(String.format("Skipped logs: %d", logsSkipped));
			}
		}
	}

}
