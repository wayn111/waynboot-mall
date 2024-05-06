package com.wayn.common.response;

import com.wayn.common.core.entity.system.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户个人资料接口
 */
@Data
public class UserProfileResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 910123559852036254L;

    /**
     * 用户详情
     */
    private User user;

    /**
     * 用户角色名列表
     */
    private String roleGroup;
}
