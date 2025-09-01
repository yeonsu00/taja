package com.taja.global.exception;

import lombok.Getter;

@Getter
public class StationStatusApiException extends RuntimeException {
    private final String code;

    public StationStatusApiException(String code, String message) {
        super(message);
        this.code = code;
    }

}
