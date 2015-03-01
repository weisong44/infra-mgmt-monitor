package com.weisong.infra.monitor.receiver;

public class LogWriterUtil {

	static public String getAppName(String path) {
		return path.split("/")[1];
	}
	
	static public StringBuffer getMetricName(String camalcased) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < camalcased.length(); i++) {
			Character c = camalcased.charAt(i);
			if(Character.isUpperCase(c)) {
				sb.append('.').append(Character.toLowerCase(c));
			}
			else {
				sb.append(c);
			}
		}
		return sb;
	}

}
