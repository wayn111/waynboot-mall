package com.wayn.common.wapper.epay.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: waynaqua
 * @date: 2024/4/30 16:05
 */
public class EpaySignUtil {

    public static String sign(Map<String, Object> map, String key) {
        Map<String, Object> params = new HashMap<>();
        map.forEach((s, o) -> {
            if (!"sign".equals(s) && !"sign_type".equals(s)) {
                params.put(s, o);
            }
        });
        String sortJoin = MapUtil.sortJoin(params, "&", "=", true, key);
        return SecureUtil.md5(sortJoin).toLowerCase();
    }
}
