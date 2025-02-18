package com.wayn.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileUtil {

    @Autowired
    private Environment environment;

    public String getCurrentProfile() {
        return environment.getActiveProfiles()[0];
    }

    public Boolean isProd() {
        return "prod".equals(environment.getActiveProfiles()[0]);
    }
}
