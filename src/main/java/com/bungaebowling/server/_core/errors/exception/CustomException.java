package com.bungaebowling.server._core.errors.exception;

import com.bungaebowling.server._core.utils.ApiUtils;

public abstract class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }

    public abstract Integer status();

    public ApiUtils.Response<?> body() {
        return ApiUtils.error(getMessage(), status());
    }

}
