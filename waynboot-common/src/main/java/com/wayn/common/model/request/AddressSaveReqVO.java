package com.wayn.common.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户地址保存请求。
 */
@Data
public class AddressSaveReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6186860685841539383L;

    private Long id;
    private String name;
    private String province;
    private String city;
    private String county;
    private String addressDetail;
    private String areaCode;
    private String postalCode;
    private String tel;
    @JsonAlias("default")
    private Boolean isDefault;
}
