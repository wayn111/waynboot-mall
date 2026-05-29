package com.wayn.data.elastic.config;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    RestClientAutoConfiguration.class
            ))
            .withUserConfiguration(ElasticConfig.class, ElasticClientConfig.class)
            .withPropertyValues(
                    "es.config.host=127.0.0.1",
                    "es.config.port=9200",
                    "es.config.scheme=http",
                    "es.config.username=elastic",
                    "es.config.password=secret"
            );

    @Test
    void restClientAutoConfigurationAndElasticsearchClientBuilderCanCoexist() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestHighLevelClient.class);
            assertThat(context).hasBean("restClientBuilder");
            assertThat(context).hasBean("elasticsearchRestClientBuilder");
            assertThat(context.getBean("restClientBuilder")).isInstanceOf(RestClient.Builder.class);
            assertThat(context.getBean("elasticsearchRestClientBuilder")).isInstanceOf(RestClientBuilder.class);
        });
    }
}
