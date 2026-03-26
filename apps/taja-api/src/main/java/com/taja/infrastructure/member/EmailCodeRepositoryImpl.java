package com.taja.infrastructure.member;

import com.taja.global.exception.EmailException;
import com.taja.application.member.EmailCodeRepository;
import com.taja.domain.member.EmailCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailCodeRepositoryImpl implements EmailCodeRepository {

    private final EmailCodeJpaRepository emailCodeJpaRepository;


    @Override
    public void saveEmailCode(EmailCode emailCode) {
        Optional<EmailCode> optionalEmailCode =
                emailCodeJpaRepository.findByEmail(emailCode.getEmail());

        optionalEmailCode.ifPresentOrElse(
                existingEmailCode
                        -> existingEmailCode.update(emailCode.getCode(), emailCode.getCreatedAt(), emailCode.getExpiresAt()),
                () -> emailCodeJpaRepository.save(emailCode)
        );
    }

    @Override
    public EmailCode findEmailCode(String email, String code) {
        return emailCodeJpaRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new EmailException("이메일 인증 코드가 일치하지 않습니다."));
    }

    @Override
    public void deleteEmailCodeById(Long emailCodeId) {
        emailCodeJpaRepository.deleteById(emailCodeId);
    }
}
