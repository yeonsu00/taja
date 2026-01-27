package com.taja.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

    OK(HttpStatus.OK, "SUCCESS"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),

    READ_FILE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "READ_FILE_ERROR"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND"),
    REFRESH_TOKEN_ERROR(HttpStatus.NOT_FOUND, "REFRESH_TOKEN_ERROR"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND"),
    EMAIL_ERROR(HttpStatus.BAD_REQUEST, "EMAIL_ERROR"),
    DUPLICATE_MEMBER(HttpStatus.BAD_REQUEST, "DUPLICATE_MEMBER"),
    TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN_ERROR"),
    FAVORITE_STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE_STATION_NOT_FOUND"),
    DUPLICATE_FAVORITE_STATION(HttpStatus.BAD_REQUEST, "DUPLICATE_FAVORITE_STATION"),
    ALREADY_JOINED(HttpStatus.CONFLICT, "ALREADY_JOINED"),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "INVALID_CONTENT"),
    NOT_STATION_MEMBER(HttpStatus.FORBIDDEN, "NOT_STATION_MEMBER"),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "INVALID_SORT_TYPE")
    ;

    private final HttpStatus httpStatus;
    private final String code;

    ResponseCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

}
