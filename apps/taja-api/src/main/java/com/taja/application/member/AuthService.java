package com.taja.application.member;

import com.taja.application.favorite.FavoriteStationRepository;
import com.taja.global.exception.EmailException;
import com.taja.global.exception.InvalidPasswordException;
import com.taja.infrastructure.jwt.JwtTokenProvider;
import com.taja.domain.member.EmailCode;
import com.taja.domain.member.EmailForm;
import com.taja.domain.member.RefreshToken;
import com.taja.domain.member.Member;
import com.taja.interfaces.api.member.response.TokenResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final FavoriteStationRepository favoriteStationRepository;
    private final EmailService emailService;
    private final EmailCodeRepository emailCodeRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
    public void signup(String name, String email, String password) {
        Member member = Member.of(name, email, passwordEncoder.encode(password));
        memberRepository.save(member);
        Counter.builder("auth.signup.total").register(meterRegistry).increment();
    }

    @Transactional
    public TokenDto login(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenDto tokenDto = generateTokenDto(authentication);

        RefreshToken refreshTokenDomain = RefreshToken.of(authentication.getName(), tokenDto.refreshToken());
        refreshTokenRepository.save(refreshTokenDomain);

        Counter.builder("auth.login.total").register(meterRegistry).increment();
        return tokenDto;
    }

    private TokenDto generateTokenDto(Authentication authentication) {
        long now = (new Date()).getTime();

        String accessToken = jwtTokenProvider.generateAccessToken(authentication, now);
        String refreshToken = jwtTokenProvider.generateRefreshToken(now);

        return new TokenDto(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshTokenValue) {
        jwtTokenProvider.validateToken(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenRepository.findByValue(refreshTokenValue);

        Member member = memberRepository.findByEmail(refreshToken.getKey());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new User(member.getEmail(), member.getPassword(), Collections.singleton(new SimpleGrantedAuthority(
                        member.getRole().name()))),
                "",
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name()))
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication, (new Date()).getTime());

        return new TokenResponse(newAccessToken);
    }

    public void sendCodeToEmail(String email) {
        String authCode = emailService.createCode();
        EmailForm emailForm = EmailForm.EMAIL_AUTH;

        String title = emailForm.getTitle();
        String content = emailForm.getContent(authCode);

        try {
            LocalDateTime createdAt = emailService.sendEmail(email, title, content);
            emailService.saveEmailCode(email, authCode, createdAt);
        } catch (RuntimeException | MessagingException e) {
            throw new EmailException("인증코드 요청에 실패했습니다.");
        }
    }

    @Transactional
    public void verifyEmailCode(String email, String code) {
        EmailCode emailCode = emailCodeRepository.findEmailCode(email, code);

        if (!emailCode.isValid()) {
            throw new EmailException("유효하지 않은 인증코드입니다.");
        }

        emailCodeRepository.deleteEmailCodeById(emailCode.getEmailCodeId());
    }

    @Transactional
    public boolean checkMemberNameDuplicate(String name) {
        return memberRepository.existsByName(name);
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByValue(refreshToken);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional
    public void withdraw(String email, String password) {
        Member member = memberRepository.findByEmail(email);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        favoriteStationRepository.deleteByMember(member);
        refreshTokenRepository.deleteByKey(email);
        memberRepository.deleteByEmail(email);
    }

    public List<Member> findMembersByEmailPrefix(String prefix) {
        return memberRepository.findByEmailStartingWith(prefix);
    }

    @Transactional
    public void deleteAllMembersById(List<Long> memberIds) {
        memberRepository.deleteAllByIdIn(memberIds);
    }

    @Transactional
    public void deleteRefreshTokensByKeys(List<String> keys) {
        refreshTokenRepository.deleteByKeyIn(keys);
    }
}
