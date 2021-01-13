package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("shop_address")
@EqualsAndHashCode(callSuper = true)
public class Address extends ShopBaseEntity implements Serializable {
    private static final long serialVersionUID = 6379853773090753607L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收货人名称
     */
    private String name;

    /**
     * 用户表的用户ID
     */
    private Long memberId;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区县
     */
    private String county;

    /**
     * 详细收货地址
     */
    private String addressDetail;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 联系电话
     */
    private String tel;

    /**
     * 是否默认选中
     */
    private boolean isDefault;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
