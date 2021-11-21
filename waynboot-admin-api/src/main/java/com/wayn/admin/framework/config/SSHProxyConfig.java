package com.wayn.admin.framework.config;

import com.wayn.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;


/**
 * ssh使用端口转发
 */
@Slf4j
@Component
public class SSHProxyConfig {

    public static final String localHost = "localhost";
    private SSHClient client;
    private SSHProxyProperties sshProxyProperties;

    public SSHProxyConfig(SSHProxyProperties sshProxyProperties) {
        this.sshProxyProperties = sshProxyProperties;
    }

    @PostConstruct
    public void connect() {

        if (!sshProxyProperties.isEnabled()) {
            return;
        }
        new Thread(() -> {
            try {
                log.info(Constants.LOG_PREFIX + "ssh connect");
                client = new SSHClient();
                client.addHostKeyVerifier(new PromiscuousVerifier());
                client.connect(sshProxyProperties.getHost(), sshProxyProperties.getPost());
                client.loadKnownHosts();

                client.authPassword(sshProxyProperties.getUserName(), sshProxyProperties.getPassword());

                final Parameters params
                        = new Parameters(localHost, sshProxyProperties.getLocalPort(),
                        sshProxyProperties.getRemoteHost(),
                        sshProxyProperties.getRemotePort());
                final ServerSocket ss = new ServerSocket();
                try (ss) {
                    ss.setReuseAddress(true);
                    ss.bind(new InetSocketAddress(params.getLocalHost(), params.getLocalPort()));
                    client.newLocalPortForwarder(params, ss).listen();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, "ssh-connect").start();

    }

    @PreDestroy
    public void close() {
        try {
            if (client != null) {
                log.info(Constants.LOG_PREFIX + "ssh disconnect");
                client.disconnect();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
