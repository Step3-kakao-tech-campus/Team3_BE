package com.bungaebowling.server._core.errors.exception.server;

import com.bungaebowling.server._core.errors.exception.CustomException;
import org.springframework.http.HttpStatus;

public class Exception500 extends CustomException {
    public Exception500(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
