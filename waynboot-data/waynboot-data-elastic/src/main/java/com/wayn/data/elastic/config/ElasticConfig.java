package com.wayn.data.elastic.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "es.config")
public class ElasticConfig {
    public String host;
    public int port;
    public String scheme;
    public String username;
    public String password;
    public int shards;
    public int replicas;
}
