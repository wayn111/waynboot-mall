package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 移动端用户头像响应。
 */
@Data
public class MobileUserAvatarResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6760446794985713348L;

    private Long userId;
    private String avatar;
}
