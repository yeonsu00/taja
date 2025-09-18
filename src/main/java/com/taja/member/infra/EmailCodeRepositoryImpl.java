package com.taja.member.infra;

import com.taja.global.exception.EmailException;
import com.taja.member.application.EmailCodeRepository;
import com.taja.member.domain.EmailCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailCodeRepositoryImpl implements EmailCodeRepository {

    private final EmailCodeJpaRepository emailCodeJpaRepository;


    @Override
    public void saveEmailCode(EmailCode emailCode) {
        Optional<EmailCodeEntity> optionalEmailCodeEntity =
                emailCodeJpaRepository.findByEmail(emailCode.getEmail());

        optionalEmailCodeEntity.ifPresentOrElse(
                existingEmailCode
                        -> existingEmailCode.update(emailCode.getCode(), emailCode.getCreatedAt(), emailCode.getExpiresAt()),
                () -> {
                    EmailCodeEntity newEmailCode = EmailCodeEntity.fromEmailCode(emailCode);
                    emailCodeJpaRepository.save(newEmailCode);
                }
        );
    }

    @Override
    public EmailCode findEmailCode(String email, String code) {
        EmailCodeEntity emailCodeEntity = emailCodeJpaRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new EmailException("이메일 인증 코드가 일치하지 않습니다."));
        return emailCodeEntity.toEmailCode();
    }

    @Override
    public void deleteEmailCodeById(Long emailCodeId) {
        emailCodeJpaRepository.deleteById(emailCodeId);
    }
}
