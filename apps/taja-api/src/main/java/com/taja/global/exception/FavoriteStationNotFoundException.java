package com.taja.global.exception;

public class FavoriteStationNotFoundException extends RuntimeException {
    public FavoriteStationNotFoundException(String message) {
        super(message);
    }
}
