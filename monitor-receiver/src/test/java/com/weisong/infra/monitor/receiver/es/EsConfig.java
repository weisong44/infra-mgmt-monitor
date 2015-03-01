package com.weisong.infra.monitor.receiver.es;

public class EsConfig {
	
	final static public String clusterName = "weisong-es";
	final static public String[] hostAndPorts = new String[] {
		"192.168.1.42:9300"
	  ,	"192.168.1.42:9300"
	  ,	"192.168.1.42:9300"
	};
	
	final static public String indexMetrics = "metrics";
	final static public String indexMgmtInfo = "mgmt-info";
	
	final static public String indexType = "data";

}
