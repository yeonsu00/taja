package com.taja.member.application;

import com.taja.member.domain.EmailCode;

public interface EmailCodeRepository {
    void saveEmailCode(EmailCode emailCode);
}
