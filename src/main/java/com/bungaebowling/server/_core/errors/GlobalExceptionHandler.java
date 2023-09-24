package com.bungaebowling.server._core.errors;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception401;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.errors.exception.server.Exception500;
import com.bungaebowling.server._core.utils.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customError(CustomException e) {
        return ResponseEntity.status(e.status()).body(e.body());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknownServerError(Exception e) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var response = ApiUtils.error(e.getMessage(), status);
        return ResponseEntity.status(status).body(response);
    }

}
