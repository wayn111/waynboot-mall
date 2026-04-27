package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 移动端用户信息响应。
 */
@Data
public class MobileUserInfoResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4299645511807000847L;

    private Long id;
    private Integer gender;
    private LocalDate birthday;
    private String email;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer userLevel;
    private String nickname;
    private String mobile;
    private String avatar;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
