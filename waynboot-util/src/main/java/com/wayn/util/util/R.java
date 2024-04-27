package com.wayn.util.util;

import com.wayn.util.enums.ReturnCodeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;

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
    private T data;

    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.code = ReturnCodeEnum.SUCCESS.getCode();
        r.msg = ReturnCodeEnum.SUCCESS.getMsg();
        return r;
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = ReturnCodeEnum.SUCCESS.getCode();
        r.msg = ReturnCodeEnum.SUCCESS.getMsg();
        r.data = data;
        return r;
    }

    public static <T> R<T> error() {
        R<T> r = new R<>();
        r.code = ReturnCodeEnum.ERROR.getCode();
        r.msg = ReturnCodeEnum.ERROR.getMsg();
        return r;
    }

    public static <T> R<T> result(boolean b) {
        return b ? R.success() : R.error();
    }

    public static <T> R<T> result(boolean b, ReturnCodeEnum returnCodeEnum) {
        return b ? R.success() : R.error(returnCodeEnum);
    }


    public static <T> R<T> error(ReturnCodeEnum returnCodeEnum) {
        R<T> r = new R<>();
        r.code = returnCodeEnum.getCode();
        r.msg = returnCodeEnum.getMsg();
        return r;
    }

    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("msg", msg)
                .append("data", data)
                .toString();
    }
}
