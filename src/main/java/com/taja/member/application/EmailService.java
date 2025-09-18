package com.taja.member.application;

import com.taja.global.exception.EmailException;
import com.taja.member.domain.EmailCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final EmailCodeRepository emailCodeRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final long EXPIRATION_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username}")
    private String username;

    public String createCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    public LocalDateTime sendEmail(String toEmail, String title, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setFrom(username);
        helper.setSubject(title);
        helper.setText(content, true);

        try {
            emailSender.send(message);
            return LocalDateTime.now();
        } catch (MailException e) {
            throw new EmailException("이메일 인증 요청에 실패했습니다.");
        }
    }

    @Transactional
    public void saveEmailCode(String email, String authCode, LocalDateTime createdAt) {
        LocalDateTime expiresAt = createdAt.plusMinutes(EXPIRATION_MINUTES);
        emailCodeRepository.saveEmailCode(EmailCode.of(email, authCode, createdAt, expiresAt));
    }
}
