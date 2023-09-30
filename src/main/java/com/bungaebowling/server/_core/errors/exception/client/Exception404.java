package com.bungaebowling.server._core.errors.exception.client;

import com.bungaebowling.server._core.errors.exception.CustomException;
import org.springframework.http.HttpStatus;

public class Exception404 extends CustomException {
    public Exception404(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return HttpStatus.NOT_FOUND.value();
    }
}
