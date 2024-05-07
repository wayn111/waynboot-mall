package com.wayn.data.elastic.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
        RestClientBuilder builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort(), config.getScheme()));
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
        return builder.setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider)
                .setKeepAliveStrategy((response, context) -> 1000 * 60));
        // final CredentialsProvider credentialsProvider =
        //         new BasicCredentialsProvider();
        // credentialsProvider.setCredentials(AuthScope.ANY,
        //         new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
        //
        // return RestClient.builder(
        //                 new HttpHost(config.getHost(), config.getPort(), config.getScheme()))
        //         .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
        //                 .setDefaultCredentialsProvider(credentialsProvider));
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }
}
