package com.taja.station.domain;

import com.taja.global.exception.ReadFileException;
import lombok.Getter;

@Getter
public enum OperationMode {
    LCD("LCD"),
    QR("QR"),
    LCD_QR("LCD,QR"),
    NEW("NEW");

    private final String mode;

    OperationMode(String mode) {
        this.mode = mode;
    }

    public static OperationMode fromString(String mode) {
        for (OperationMode method : OperationMode.values()) {
            if (method.getMode().equalsIgnoreCase(mode)) {
                return method;
            }
        }
        throw new ReadFileException("잘못된 운영 방식이 발견되었습니다. : " + mode);
    }
}
