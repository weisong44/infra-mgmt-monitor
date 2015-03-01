package com.weisong.infra.monitor.receiver.es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EsQueryTest {
	
	private TransportClient client;
	
	@Before
	public void setup() {
		Settings settings = ImmutableSettings.settingsBuilder()
			.put("cluster.name", EsConfig.clusterName)
			.build();
		client = new TransportClient(settings);
		for(String hostAndPort : EsConfig.hostAndPorts) {
			String[] tokens = hostAndPort.split(":");
			String host = tokens[0];
			int port = 9300; 
			if(tokens.length > 1) {
				port = Integer.valueOf(tokens[1]);
			}
			client.addTransportAddress(new InetSocketTransportAddress(host, port));
		}
	}

	@After
	public void cleanup() {
		client.close();
	}
	
	@Test
	public void testQuery() {
		// Query
		QueryBuilder query = QueryBuilders.termQuery("parent", "192.168.1.96");
		// Filter
		FilterBuilder filter = FilterBuilders.existsFilter("jmxPort");
		SearchResponse response = client.prepareSearch(EsConfig.indexMgmtInfo)
			.setTypes(EsConfig.indexType)
			.setSearchType(SearchType.DEFAULT)
			.setQuery(query) // Query
			.setPostFilter(filter) // Filter
			.setFrom(0).setSize(10).setExplain(true)
			.execute()
			.actionGet();
		for(SearchHit hit : response.getHits()) {
			System.out.println(hit.getSourceAsString());
		}
	}
}
