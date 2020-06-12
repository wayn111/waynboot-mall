package com.wayn.common.util;

import java.util.UUID;

public class IdUtil {

    public static String getUid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
