package com.wayn.monitor.config;

import com.hazelcast.config.*;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_EVENT_STORE_MAP;
import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;

@Configuration
public class WebSecurityConfigurer {
    private final String adminContextPath;

    public WebSecurityConfigurer(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        http.headers().frameOptions().disable()
                .and().authorizeHttpRequests()
                .requestMatchers(adminContextPath + "/assets/**"
                        , adminContextPath + "/login"
                        , adminContextPath + "/actuator/**"
                        , adminContextPath + "/instances/**"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage(adminContextPath + "/login")
                .successHandler(successHandler).and()
                .logout().logoutUrl(adminContextPath + "/logout")
                .and()
                .httpBasic().and()
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        "/instances",
                        "/actuator/**"
                );
        return http.build();
    }

    @Bean
    public Config hazelcastConfig() {
        // This map is used to store the events.
        // It should be configured to reliably hold all the data,
        // Spring Boot Admin will compact the events, if there are too many
        MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)
                .setBackupCount(1)
                .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

        // This map is used to deduplicate the notifications.
        // If data in this map gets lost it should not be a big issue as it will atmost
        // lead to
        // the same notification to be sent by multiple instances
        MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setBackupCount(1)
                .setEvictionConfig(
                        new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.PER_NODE))
                .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

        Config config = new Config();
        config.addMapConfig(eventStoreMap);
        config.addMapConfig(sentNotificationsMap);
        config.setProperty("hazelcast.jmx", "true");

        // WARNING: This setups a local cluster, you change it to fit your needs.
        // config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        // TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        // tcpIpConfig.setEnabled(true);
        // tcpIpConfig.setMembers(singletonList("127.0.0.1"));
        return config;
    }
}
