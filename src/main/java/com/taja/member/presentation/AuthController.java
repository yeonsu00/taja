package com.taja.member.presentation;

import com.taja.global.response.CommonApiResponse;
import com.taja.member.application.dto.TokenDto;
import com.taja.member.application.AuthService;
import com.taja.member.application.CookieService;
import com.taja.member.presentation.request.EmailRequest;
import com.taja.member.presentation.request.LoginRequest;
import com.taja.member.presentation.request.SignUpRequest;
import com.taja.member.presentation.request.VerifyEmailRequest;
import com.taja.member.presentation.response.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/signup")
    public CommonApiResponse<String> signup(@Valid @RequestBody SignUpRequest request) {
        authService.signup(request.name(), request.email(), request.password());
        return CommonApiResponse.success("회원가입에 성공했습니다.");
    }

    @PostMapping("/login")
    public CommonApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request.email(), request.password());
        cookieService.addRefreshTokenCookie(response, tokenDto.refreshToken());

        TokenResponse tokenResponse = new TokenResponse(tokenDto.accessToken());
        return CommonApiResponse.success(tokenResponse, "로그인에 성공했습니다.");
    }

    @PostMapping("/token")
    public CommonApiResponse<TokenResponse> reissueToken(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse newTokenResponse = authService.reissue(refreshToken);
        return CommonApiResponse.success(newTokenResponse, "새로운 액세스 토큰이 발급되었습니다.");
    }

    @PostMapping("/email/send")
    public CommonApiResponse<String> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        authService.sendCodeToEmail(emailRequest.email());
        return CommonApiResponse.success("이메일 인증 요청을 성공했습니다.");
    }

    @PostMapping("/email/verify")
    public CommonApiResponse<String> validateEmailCode(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
        authService.verifyEmailCode(verifyEmailRequest.email(), verifyEmailRequest.code());
        return CommonApiResponse.success("이메일 인증을 성공했습니다.");
    }

}
