package com.taja.global.exception;

import com.taja.global.response.CommonApiResponse;
import com.taja.global.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<?>> handleUnexpectedException(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof MemberException) {
            return handleMemberNotFoundException((MemberException) cause);
        }

        log.error("[UNEXPECTED ERROR] {}", ex.getMessage(), ex);
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

        return ResponseEntity.status(ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(body);
    }

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
    public ResponseEntity<CommonApiResponse<?>> handleMemberNotFoundException(MemberException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.MEMBER_NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(ResponseCode.MEMBER_NOT_FOUND.getHttpStatus()).body(body);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<CommonApiResponse<?>> handleEmailException(EmailException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.EMAIL_ERROR, ex.getMessage());

        return ResponseEntity.status(ResponseCode.EMAIL_ERROR.getHttpStatus()).body(body);
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<CommonApiResponse<?>> handleDuplicateNameException(DuplicateMemberException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.DUPLICATE_MEMBER, ex.getMessage());

        return ResponseEntity.status(ResponseCode.DUPLICATE_MEMBER.getHttpStatus()).body(body);
    }

    @ExceptionHandler(FavoriteStationNotFoundException.class)
    public ResponseEntity<CommonApiResponse<?>> handleFavoriteStationNotFoundException(FavoriteStationNotFoundException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.FAVORITE_STATION_NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(ResponseCode.FAVORITE_STATION_NOT_FOUND.getHttpStatus()).body(body);
    }

    @ExceptionHandler(DuplicateFavoriteStationException.class)
    public ResponseEntity<CommonApiResponse<?>> handleDuplicateFavoriteStationException(DuplicateFavoriteStationException ex) {
        CommonApiResponse<?> body = CommonApiResponse.failure(ResponseCode.DUPLICATE_FAVORITE_STATION, ex.getMessage());

        return ResponseEntity.status(ResponseCode.DUPLICATE_FAVORITE_STATION.getHttpStatus()).body(body);
    }
}
