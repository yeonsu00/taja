package com.taja.member.application;

import com.taja.jwt.JwtTokenProvider;
import com.taja.member.application.dto.TokenDto;
import com.taja.member.domain.RefreshToken;
import com.taja.member.domain.Member;
import com.taja.member.presentation.response.TokenResponse;
import java.util.Collections;
import java.util.Date;
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

    @Transactional
    public void signup(String name, String email, String password) {
        Member member = Member.of(name, email, passwordEncoder.encode(password));
        memberRepository.save(member);
    }

    @Transactional
    public TokenDto login(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenDto tokenDto = generateTokenDto(authentication);

        RefreshToken refreshTokenDomain = RefreshToken.of(authentication.getName(), tokenDto.refreshToken());
        refreshTokenRepository.save(refreshTokenDomain);

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
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByValue(refreshTokenValue);

        Member member = memberRepository.findByEmail(refreshToken.getKey());

        // 4. 해당 사용자 정보로 Authentication 객체 생성
        //    (이 부분은 CustomUserDetailsService를 직접 사용하거나, 아래처럼 수동으로 만들 수 있습니다)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new User(member.getEmail(), member.getPassword(), Collections.singleton(new SimpleGrantedAuthority(
                        member.getRole().name()))),
                "",
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name()))
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication, (new Date()).getTime());

        return new TokenResponse(newAccessToken);
    }

}
