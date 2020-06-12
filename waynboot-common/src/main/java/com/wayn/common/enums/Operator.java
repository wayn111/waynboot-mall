package com.wayn.common.enums;

/**
 * 日志操作记录
 *
 * @author wayn
 */
public enum Operator {
    ADD("新建", "add"),
    UPDATE("更新", "update"),
    DELETE("删除", "delete"),
    SELECT("查询", "select"),
    UPLOAD("上传", "upload"),
    DOWNLOAD("下载", "download"),
    OTHER("其他", "other"),
    LOGIN("登陆", "login"),
    LOGOUT("登出", "logout"),
    FILE_OPERATE("文件操作", "fileOperate"),
    GEN_CODE("代码生成", "genCode"),
    EXECUTOR("执行", "executor");

    String name;
    String code;

    Operator(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public Operator setCode(String code) {
        this.code = code;
        return this;
    }
}
