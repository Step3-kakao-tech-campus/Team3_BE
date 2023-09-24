package com.bungaebowling.server._core.errors.exception.client;

import com.bungaebowling.server._core.errors.exception.CustomException;
import org.springframework.http.HttpStatus;

public class Exception401 extends CustomException {
    public Exception401(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
