package com.bungaebowling.server._core.errors;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customError(CustomException e) {
        log.error("에러 디버깅", e);
        return ResponseEntity.status(e.status()).body(e.body());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknownServerError(Exception e) {
        log.error("unknown 에러 발생", e);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var response = ApiUtils.error(e.getMessage(), ErrorCode.UNKNOWN_SERVER_ERROR);
        return ResponseEntity.status(status).body(response);
    }

}
