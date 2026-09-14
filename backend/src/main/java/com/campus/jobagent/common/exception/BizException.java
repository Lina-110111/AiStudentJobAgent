package com.campus.jobagent.common.exception;

import com.campus.jobagent.common.api.ResultCode;

/**
 * 业务异常：由业务代码主动抛出，统一被 {@link GlobalExceptionHandler} 捕获。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public int getCode() {
        return code;
    }

    public static BizException of(ResultCode resultCode) {
        return new BizException(resultCode);
    }

    /** 使用业务状态码 + 自定义提示构造异常。 */
    public static BizException of(ResultCode resultCode, String message) {
        return new BizException(resultCode, message);
    }
}
