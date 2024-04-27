package com.wayn.common.core.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志表
 * @TableName sys_log
 */
@TableName(value ="sys_log")
@Data
public class SysLog implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 操作状态 1 正常 0失败
     */
    private Integer operState;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 请求名称
     */
    private String method;

    /**
     * 请求路径
     */
    private String url;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求类型 post/get
     */
    private String requestMethod;

    /**
     * 请求响应
     */
    private String requestResponse;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 请求ip
     */
    private String ip;

    /**
     * 执行耗时，单位毫秒
     */
    private Long executeTime;

    /**
     * 日志时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
