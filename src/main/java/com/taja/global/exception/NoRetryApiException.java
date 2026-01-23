package com.taja.global.exception;

import lombok.Getter;

@Getter
public class NoRetryApiException extends RuntimeException {
    private final String code;

    public NoRetryApiException(String code, String message) {
        super(message);
        this.code = code;
    }
}
