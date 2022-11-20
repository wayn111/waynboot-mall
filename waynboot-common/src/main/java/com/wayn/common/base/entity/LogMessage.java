package com.wayn.common.base.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.StringJoiner;

@Getter
@Setter
public class LogMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 3330416032656611600L;

    private Integer httpStatus;
    private String httpMethod;
    private String path;
    private String reqParameter;

    private long executionTime;
    private String clientIp;
    private String javaMethod;
    private String response;

    @Override
    public String toString() {
        return new StringJoiner(", ", LogMessage.class.getSimpleName() + "[", "]")
                .add("httpStatus=" + httpStatus)
                .add("httpMethod='" + httpMethod + "'")
                .add("path='" + path + "'")
                .add("reqParameter=" + reqParameter)
                .add("executionTime=" + executionTime)
                .add("clientIp='" + clientIp + "'")
                .add("javaMethod='" + javaMethod + "'")
                .add("response='" + response + "'")
                .toString();
    }
}
