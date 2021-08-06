package com.wayn.data.elastic.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * elastic异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ElasticException extends RuntimeException {

    private static final long serialVersionUID = 9005063206372011860L;

    private Integer code;

    private String msg;

    public ElasticException(String msg, Integer code) {
        this.code = code;
        this.msg = msg;
    }

    public ElasticException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticException(String message) {
        super(message);
    }

}
