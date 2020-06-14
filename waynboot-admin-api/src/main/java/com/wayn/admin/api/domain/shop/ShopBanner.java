package com.wayn.admin.api.domain.shop;

import com.wayn.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopBanner extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6208877431925919530L;

    private Long id;

    private String idFile;

    private String page;

    private String url;

    private Integer sort;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private Integer delFlag;
}
