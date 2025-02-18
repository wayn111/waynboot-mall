package com.wayn.common.response;

import lombok.Data;

/**
 * @author: waynaqua
 * @date: 2024/6/28 18:16
 */
@Data
public class JsSdkInitResVO {

    private String appId;
    private String timestamp;
    private String nonceStr;
    private String signature;
}
