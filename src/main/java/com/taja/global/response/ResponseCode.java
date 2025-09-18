package com.taja.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    OK(HttpStatus.OK, "SUCCESS"),

    READ_FILE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "READ_FILE_ERROR"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND"),
    REFRESH_TOKEN_ERROR(HttpStatus.NOT_FOUND, "REFRESH_TOKEN_ERROR"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    EMAIL_ERROR(HttpStatus.BAD_REQUEST, "EMAIL_ERROR")
    ;

    private final HttpStatus httpStatus;
    private final String code;

    ResponseCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

}
