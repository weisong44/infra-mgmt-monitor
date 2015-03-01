package com.weisong.infra.monitor.receiver.opentsdb;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.infra.monitor.common.MonitoringData;
import com.weisong.infra.monitor.receiver.LogWriter;
import com.weisong.infra.monitor.receiver.LogWriterUtil;

public class OpenTsdbLogWriter implements LogWriter {
	
	final static public long MAX_CONN_IDLE_TIME = 60000L;
	final static public long RECONNECT_INTERVAL = 5000L;
	
	private class ConnWatcher extends Thread {
		
		private boolean shutdown;
		
		public void run() {
			setName(getClass().getSimpleName());
			long lastDataReceived = Long.MAX_VALUE;
			boolean shouldSleep = false;
			while(shutdown == false) {
				try {
					if(os != null && System.currentTimeMillis() - lastDataReceived > 60000) {
						try {
							logger.info(String.format(
								"Connection idle for more than %d ms, disconnect.",
								MAX_CONN_IDLE_TIME));
							close(is, os, socket); 
							os = null;
						}
						catch(Throwable t) {
							t.printStackTrace();
						}
					}
					
					if(os != null) {
						os.println("version");
						while(is.ready()) {
							is.readLine();
							lastDataReceived = System.currentTimeMillis();
						}
						Thread.sleep(5000);
						continue;
					}
					else {
						if(shouldSleep) {
							Thread.sleep(5000);
							shouldSleep = false;
						}
						socket = new Socket(server, Integer.valueOf(port));
						os = new PrintStream(socket.getOutputStream());
						is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						lastDataReceived = System.currentTimeMillis();
						logger.info(String.format("Connected to OpenTSDB at %s:%s", server, port));
					}
				} catch (NumberFormatException e) {
					logger.error("Bad port number: " + e.getMessage());
					System.exit(-1);
				} catch (UnknownHostException e) {
					logger.error("Couldn't establish connection: " + e.getMessage());
					System.exit(1);
				} catch (Exception e) {
					logger.error(String.format(
						"Failed to connect to server, retry in %s ms: %s", 
						RECONNECT_INTERVAL, e.getMessage()));
					shouldSleep = true;
				}
				
			}
		}
		
		private void close(Closeable ... array) {
			for(Closeable c : array) {
				try {
					if(c != null) {
						c.close();
					}
				}
				catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
		
		private void shutdown() {
			this.shutdown = true;
		}
	}
	
	final static public SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z");
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String server = "localhost";
	private int port = 4242;
	
	private ConnWatcher connWatcher;
	
	private PrintStream os;
	private BufferedReader is;
	private Socket socket;

	private long logsWritten;
	private long logsSkipped;
	
	public OpenTsdbLogWriter(String server, int port) {
		this.server = server;
		this.port = port;
		logger.info(String.format("Using server %s:%s", server, port));
	}

	public void start() {
		connWatcher = new ConnWatcher();
		connWatcher.start();
		logger.info("Started");
	}
	
	public void stop() {
		if(connWatcher != null) {
			connWatcher.shutdown();
		}
		logger.info("Stopped");
	}
	
	public boolean isConnected() {
		return os != null;
	}
	
	StringBuffer appendTag(StringBuffer sb, String tag, String value) {
		return sb.append(" ").append(tag).append("=").append(value);
	}
	
	@Override
	public void write(MonitoringData data) throws Exception {

		long timestamp = df.parse(data.getTimestamp()).getTime();
		
		StringBuffer tags = new StringBuffer();
		appendTag(tags, "host", data.getHostname());
		appendTag(tags, "addr", data.getIpAddr());
		appendTag(tags, "app", LogWriterUtil.getAppName(data.getPath()));
		appendTag(tags, "path", data.getPath());
		appendTag(tags, "module", data.getName());

		for(String metric : data.getCounters().keySet()) {
			StringBuffer sb = new StringBuffer("put");
			sb.append(' ').append(LogWriterUtil.getMetricName(metric));
			sb.append(' ').append(timestamp);
			sb.append(' ').append(data.getCounters().get(metric));
			sb.append(' ').append(tags);
			String cmd = sb.toString();
			
			logger.debug(cmd);
			
			if(os != null) {
				os.println(cmd);
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

}
