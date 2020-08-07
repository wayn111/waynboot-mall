package com.wayn.common.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class R {

    private int code;
    private String msg;
    private Map<String, Object> map = new HashMap<>();

    public static R success() {
        R r = new R();
        r.code = 200;
        r.msg = "请求成功";
        return r;
    }

    public static R success(String msg) {
        R r = new R();
        r.code = 200;
        r.msg = msg;
        return r;
    }

    public static R error() {
        R r = new R();
        r.code = 500;
        r.msg = "请求失败";
        return r;
    }

    public static R result(boolean b) {
        if (b) return R.success();
        return R.error();
    }

    public static R result(boolean b, String msg) {
        if (b) return R.success();
        return R.error(msg);
    }

    public static R error(String msg) {
        R r = new R();
        r.code = 500;
        r.msg = msg;
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public R add(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("msg", msg)
                .append("map", map)
                .toString();
    }
}
