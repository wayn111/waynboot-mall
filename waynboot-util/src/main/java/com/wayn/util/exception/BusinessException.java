package com.wayn.util.exception;

import com.wayn.util.enums.ReturnCodeEnum;
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

    public BusinessException(ReturnCodeEnum returnCodeEnum) {
        super(returnCodeEnum.getMsg());
        this.code = returnCodeEnum.getCode();
        this.msg = returnCodeEnum.getMsg();
    }

    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
        this.code = ReturnCodeEnum.CUSTOM_ERROR.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
