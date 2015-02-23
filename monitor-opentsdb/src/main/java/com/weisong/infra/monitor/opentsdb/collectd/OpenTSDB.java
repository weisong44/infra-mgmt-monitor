package com.weisong.infra.monitor.opentsdb.collectd;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.collectd.api.Collectd;
import org.collectd.api.CollectdConfigInterface;
import org.collectd.api.CollectdInitInterface;
import org.collectd.api.CollectdWriteInterface;
import org.collectd.api.DataSource;
import org.collectd.api.OConfigItem;
import org.collectd.api.OConfigValue;
import org.collectd.api.ValueList;

public class OpenTSDB 
		implements CollectdWriteInterface, CollectdInitInterface, CollectdConfigInterface {
	
	final static public long MAX_CONN_IDLE_TIME = 60000L;
	final static public long RECONNECT_INTERVAL = 5000L;
	
	private class ConnWatcher extends Thread {
		public void run() {
			setName(getClass().getSimpleName());
			long lastDataReceived = Long.MAX_VALUE;
			boolean shouldSleep = false;
			while(true) {
				try {
					if(os != null && System.currentTimeMillis() - lastDataReceived > 60000) {
						try {
							Log.logInfo(String.format(
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
						Log.logInfo(String.format("Connected to OpenTSDB at %s:%s", server, port));
					}
				} catch (NumberFormatException e) {
					Log.logError("Bad port number: " + e.getMessage());
					System.exit(-1);
				} catch (UnknownHostException e) {
					Log.logError("Couldn't establish connection: " + e.getMessage());
					System.exit(1);
				} catch (Exception e) {
					Log.logError(String.format(
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
	}
	
	static private class Log {
		static private void logDebug(String msg) {
			if(testMode)
				System.out.println(msg);
			else
				Collectd.logDebug(msg);
		}
		static private void logInfo(String msg) {
			if(testMode)
				System.out.println(msg);
			else
				Collectd.logInfo(msg);
		}
		static private void logError(String msg) {
			if(testMode)
				System.out.println(msg);
			else
				Collectd.logError(msg);
		}
	}
	
	static public boolean testMode = false;
	
	private String server = "localhost";
	private String port = "4242";
	private PrintStream os;
	private BufferedReader is;
	private Socket socket;

	public OpenTSDB() {
		if(!testMode) {
			Collectd.registerInit("OpenTSDB", this);
			Collectd.registerWrite("OpenTSDB", this);
			Collectd.registerConfig("OpenTSDB", this);
		}
	}

	public int init() {
		Log.logInfo(String.format("Collectd OpenTSDB plugin: using server %s:%s", server, port));
		new ConnWatcher().start();
		return 0;
	}

	public int write(ValueList valueList) {
		List<DataSource> ds = valueList.getDataSet().getDataSources();
		List<Number> values = valueList.getValues();
		int size = values.size();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < size; i++) {
			// Buffer
			sb.setLength(0);
			sb.append("put ");

			// Metric name
			String name, pointName, plugin, pluginInstance, type, typeInstance;
			ArrayList<String> parts = new ArrayList<String>();
			ArrayList<String> tags = new ArrayList<String>();

			plugin = valueList.getPlugin();
			pluginInstance = valueList.getPluginInstance();
			type = valueList.getType();
			typeInstance = valueList.getTypeInstance();

			Log.logDebug("plugin: " + plugin + " pluginInstance: "
					+ pluginInstance + " type: " + type + " typeInstance: "
					+ typeInstance);

			// FIXME: refactor to switch?
			if (plugin != null && !plugin.isEmpty()) {
				parts.add(plugin);
				if (pluginInstance != null && !pluginInstance.isEmpty()) {
					tags.add(plugin + "_instance=" + pluginInstance);
				}
				if (type != null && !type.isEmpty()) {
					tags.add(plugin + "_type=" + type);
				}
				if (typeInstance != null && !typeInstance.isEmpty()) {
					tags.add(plugin + "_type_instance=" + typeInstance);
				}

				pointName = ds.get(i).getName();
				if (!pointName.equals("value")) {
					// LogWrapper.logInfo("pointName: " + pointName);
					tags.add(plugin + "_point=" + pointName);
				}
			}

			name = join(parts, ".");

			sb.append(name).append(' ');

			// Time
			long time = valueList.getTime() / 1000;
			sb.append(time).append(' ');

			// Value
			Number val = values.get(i);
			sb.append(val).append(' ');

			// Host
			String host = valueList.getHost();
			sb.append("host=").append(host).append(" ");

			// Meta
			sb.append("source=collectd");

			sb.append(" ").append(join(tags, " "));

			String output = sb.toString();

			// Send to OpenTSDB
			//Log.logInfo(output);
			if(os != null) {
				os.println(output);
			}
		}

		return (0);
	}

	public static String join(Collection<String> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iter = s.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	public int config(OConfigItem ci) /* {{{ */
	{
		List<OConfigItem> children;
		int i;

		Log.logDebug("OpenTSDB plugin: config: ci = " + ci + ";");

		children = ci.getChildren();
		for (i = 0; i < children.size(); i++) {
			List<OConfigValue> values;
			OConfigItem child;
			String key;

			child = children.get(i);
			key = child.getKey();
			if (key.equalsIgnoreCase("Server")) {
				values = child.getValues();
				if (values.size() != 2) {
					Log.logError("OpenTSDB plugin: "
							+ key
							+ "configuration option needs exactly two arguments: server + port");
					return (1);
				} else {
					server = values.get(0).toString();
					port = values.get(1).toString();
				}
			} else {
				Log.logError("OpenTSDB plugin: Unknown config option: "
						+ key);
			}
		}

		return (0);
	}
}
