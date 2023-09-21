package com.bungaebowling.server._core.errors.exception.client;

import com.bungaebowling.server._core.errors.exception.CustomException;
import org.springframework.http.HttpStatus;

public class Exception403 extends CustomException {
    public Exception403(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return HttpStatus.FORBIDDEN.value();
    }
}
