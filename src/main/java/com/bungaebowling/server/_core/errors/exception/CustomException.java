package com.bungaebowling.server._core.errors.exception;

import com.bungaebowling.server._core.utils.ApiUtils;


public class CustomException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public CustomException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public Integer status() {
        return errorCode.getHttpStatus().value();
    }

    public ApiUtils.Response<?> body() {
        return ApiUtils.error(message, errorCode, status());
    }
}
