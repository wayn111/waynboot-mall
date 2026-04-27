package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户地址响应。
 */
@Data
public class AddressResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3554670977437796757L;

    private Long id;
    private String name;
    private String province;
    private String city;
    private String county;
    private String addressDetail;
    private String areaCode;
    private String postalCode;
    private String tel;
    private Boolean isDefault;
}
