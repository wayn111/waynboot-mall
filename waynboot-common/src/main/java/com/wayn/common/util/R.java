package com.wayn.common.util;

import com.alibaba.fastjson2.JSONObject;
import com.wayn.common.enums.ReturnCodeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -5316597326293972581L;
    /**
     * 返回状态码：返回200表示请求接口成功，非200表示请求接口失败
     */
    private int code;
    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private Map<String, T> map = new HashMap<>();

    public static R success() {
        R r = new R();
        r.code = ReturnCodeEnum.SUCCESS.getCode();
        r.msg = ReturnCodeEnum.SUCCESS.getMsg();
        return r;
    }

    public static <T> R success(T data) {
        R r = new R();
        r.code = ReturnCodeEnum.SUCCESS.getCode();
        r.msg = ReturnCodeEnum.SUCCESS.getMsg();
        r.map = JSONObject.from(data);
        return r;
    }

    public static R error() {
        R r = new R();
        r.code = ReturnCodeEnum.ERROR.getCode();
        r.msg = ReturnCodeEnum.ERROR.getMsg();
        return r;
    }

    public static R result(boolean b) {
        return b ? R.success() : R.error();
    }

    public static R result(boolean b, ReturnCodeEnum returnCodeEnum) {
        return b ? R.success() : R.error(returnCodeEnum);
    }


    public static R error(ReturnCodeEnum returnCodeEnum) {
        R r = new R();
        r.code = returnCodeEnum.getCode();
        r.msg = returnCodeEnum.getMsg();
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public R add(String key, T value) {
        map.put(key, value);
        return this;
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
