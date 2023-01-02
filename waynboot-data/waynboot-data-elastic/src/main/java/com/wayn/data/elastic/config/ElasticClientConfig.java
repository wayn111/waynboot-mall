package com.wayn.data.elastic.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticClientConfig {
    @Bean
    public RestClientBuilder restClientBuilder(ElasticConfig config) {
        return RestClient.builder(new HttpHost(config.getHost(), config.getPort(), config.getScheme()));
    }

    @Bean
    public RestClient elasticsearchRestClient(ElasticConfig config) {
        return RestClient.builder(new HttpHost(config.getHost(), config.getPort(), config.getScheme())).build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }
}
