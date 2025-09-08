package com.taja.global.exception;

import com.taja.global.response.CommonApiResponse;
import com.taja.global.response.ResponseCode;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReadFileException.class)
    public CommonApiResponse<?> handleReadFileException(ReadFileException ex) {
        return CommonApiResponse.failure(ResponseCode.READ_FILE_ERROR, ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public CommonApiResponse<?> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return CommonApiResponse.failure(ResponseCode.INVALID_REQUEST, message);
    }

}
