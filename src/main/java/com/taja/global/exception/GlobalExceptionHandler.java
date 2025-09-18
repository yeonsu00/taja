package com.taja.global.exception;

import com.taja.global.response.CommonApiResponse;
import com.taja.global.response.ResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReadFileException.class)
    public ResponseEntity<CommonApiResponse<?>>  handleReadFileException(ReadFileException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.READ_FILE_ERROR, ex.getMessage());

        return ResponseEntity.status(ResponseCode.READ_FILE_ERROR.getHttpStatus()).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonApiResponse<?>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.INVALID_REQUEST, message);

        return ResponseEntity.status(ResponseCode.INVALID_REQUEST.getHttpStatus()).body(body);
    }

    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<CommonApiResponse<?>> handleStationNotFoundException(StationNotFoundException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.STATION_NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(ResponseCode.STATION_NOT_FOUND.getHttpStatus()).body(body);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<CommonApiResponse<?>> handleRefreshTokenException(TokenException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.REFRESH_TOKEN_ERROR, ex.getMessage());

        return ResponseEntity.status(ResponseCode.REFRESH_TOKEN_ERROR.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<CommonApiResponse<?>> handleUserNotFoundException(MemberException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.USER_NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(ResponseCode.USER_NOT_FOUND.getHttpStatus()).body(body);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<CommonApiResponse<?>> handleEmailException(EmailException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.EMAIL_ERROR, ex.getMessage());

        return ResponseEntity.status(ResponseCode.EMAIL_ERROR.getHttpStatus()).body(body);
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<CommonApiResponse<?>> handleDuplicateNameException(DuplicateNameException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.EMAIL_ERROR, ex.getMessage());

        return ResponseEntity.status(ResponseCode.EMAIL_ERROR.getHttpStatus()).body(body);
    }
}
