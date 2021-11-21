package com.wayn.admin.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wayn.ssh-proxy")
public class SSHProxyProperties {

    private boolean enabled;

    private String host;
    private Integer post;

    private String userName;
    private String password;

    private Integer localPort;

    private String remoteHost;
    private Integer remotePort;
}
