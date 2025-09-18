package com.taja.member.infra;

import com.taja.member.application.EmailCodeRepository;
import com.taja.member.domain.EmailCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailCodeRepositoryImpl implements EmailCodeRepository {

    private final EmailCodeJpaRepository emailCodeJpaRepository;


    @Override
    public void saveEmailCode(EmailCode emailCode) {
        EmailCodeEntity emailCodeEntity = EmailCodeEntity.fromEmailCode(emailCode);
        emailCodeJpaRepository.save(emailCodeEntity);
    }
}
