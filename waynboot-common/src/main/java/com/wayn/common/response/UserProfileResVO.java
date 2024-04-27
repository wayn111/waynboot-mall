package com.wayn.common.response;

import com.wayn.common.core.entity.system.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/4/27 15:43
 */
@Data
public class UserProfileResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 910123559852036254L;

    private User user;
    private String roleGroup;
}
