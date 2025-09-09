package com.taja.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    OK(HttpStatus.OK, "SUCCESS"),

    READ_FILE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "READ_FILE_ERROR"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    ;

    private final HttpStatus httpStatus;
    private final String code;

    ResponseCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

}
