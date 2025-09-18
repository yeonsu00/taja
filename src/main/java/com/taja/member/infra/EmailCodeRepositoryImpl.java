package com.taja.member.infra;

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
}
