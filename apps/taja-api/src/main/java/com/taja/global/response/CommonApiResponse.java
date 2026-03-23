package com.taja.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public class CommonApiResponse<T> {

    @JsonProperty("code")
    private final String code;

    @JsonProperty("message")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("data")
    private final T data;

    @Builder
    private CommonApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonApiResponse<T> success(String message) {
        return CommonApiResponse.<T>builder()
                .code(ResponseCode.OK.getCode())
                .message(message)
                .data(null)
                .build();
    }

    public static <T> CommonApiResponse<T> success(T data, String message) {
        return CommonApiResponse.<T>builder()
                .code(ResponseCode.OK.getCode())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> CommonApiResponse<T> failure(ResponseCode code, String message) {
        return CommonApiResponse.<T>builder()
                .code(code.getCode())
                .message(message)
                .data(null)
                .build();
    }

}
