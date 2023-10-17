package com.bungaebowling.server._core.utils;

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

    public static <T> Response<T> error(String errorMessage, T errorCode, HttpStatus status) {
        return new Response<>(status.value(), errorCode, errorMessage);
    }

    public static <T> Response<T> error(String errorMessage, T errorCode, Integer status) {
        return new Response<>(status, errorCode, errorMessage);
    }

    public record Response<T>(
            int status,
            T response,
            String errorMessage
    ) {
    }

}
