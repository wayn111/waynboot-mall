package com.wayn.common.exception;

import com.wayn.common.enums.ReturnCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 自定义业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2969542356458982180L;
    private Integer code;

    private String msg;

    public BusinessException(String msg, Integer code) {
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(ReturnCodeEnum returnCodeEnum) {
        this.code = returnCodeEnum.getCode();
        this.msg = returnCodeEnum.getMsg();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }

}
