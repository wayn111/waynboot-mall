package com.wayn.data.elastic.config;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ElasticConfig {
    @Value("${es.host}")
    public String host;
    @Value("${es.port}")
    public int port;
    @Value("${es.scheme}")
    public String scheme;
    @Value("${es.shards}")
    public int shards;
    @Value("${es.replicas}")
    public int replicas;

    @Bean
    public RestClientBuilder restClientBuilder() {
        return RestClient.builder(makeHttpHost());
    }

    @Bean
    public RestClient elasticsearchRestClient() {
        return RestClient.builder(new HttpHost(host, port, scheme)).build();
    }

    private HttpHost makeHttpHost() {
        return new HttpHost(host, port, scheme);
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }
}
