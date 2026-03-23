package com.taja.application.member;

import com.taja.domain.member.EmailCode;

public interface EmailCodeRepository {
    void saveEmailCode(EmailCode emailCode);

    EmailCode findEmailCode(String email, String code);

    void deleteEmailCodeById(Long emailCodeId);
}
