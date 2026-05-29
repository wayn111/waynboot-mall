package com.wayn.mobile.framework.config;

import cn.dev33.satoken.config.SaTokenConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileSaTokenConfigurationTest {

    @Test
    void initSaTokenConfigShouldReadRawAuthorizationHeader() {
        SaTokenConfig saTokenConfig = new SaTokenConfig();
        MobileSaTokenConfiguration configuration = new MobileSaTokenConfiguration(saTokenConfig);

        configuration.initSaTokenConfig();

        assertNull(saTokenConfig.getTokenPrefix());
        assertTrue(saTokenConfig.getIsReadHeader());
    }
}
