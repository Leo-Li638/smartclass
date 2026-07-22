package com.smartclass.common;

import lombok.Getter;

/**
 * 业务异常,携带状态码便于统一转换成响应结果
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(ResultCode.ERROR.getCode(), message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
