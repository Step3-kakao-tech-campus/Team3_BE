package com.bungaebowling.server._core.utils;

import com.bungaebowling.server._core.errors.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ApiUtils {

    public static <T> Response<T> success(T response) {
        return new Response<>(HttpStatus.OK.value(), response, null);
    }

    public static <T> Response<T> success(T response, HttpStatus status) {
        return new Response<>(status.value(), response, null);
    }

    public static <T> Response<T> success(T response, Integer status) {
        return new Response<>(status, response, null);
    }

    public static <T> Response<T> success() {
        return new Response<>(HttpStatus.OK.value(), null, null);
    }

    public static <T> Response<T> success(HttpStatus status) {
        return new Response<>(status.value(), null, null);
    }

    public static <T> Response<T> success(Integer status) {
        return new Response<>(status, null, null);
    }

    public static Response<ErrorCode> error(String errorMessage, ErrorCode errorCode) {
        return new Response<>(errorCode.getHttpStatus().value(), errorCode, errorMessage);
    }


    public record Response<T>(
            int status,
            T response,
            String errorMessage
    ) {
    }

}
