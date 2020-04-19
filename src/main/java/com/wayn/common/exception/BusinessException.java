package com.wayn.common.exception;

import lombok.Data;

/**
 * 自定义业务异常
 */
@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    private String msg;

    public BusinessException(String msg, Integer code) {
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }

}
