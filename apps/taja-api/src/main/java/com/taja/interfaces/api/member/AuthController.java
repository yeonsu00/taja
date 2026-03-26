package com.taja.interfaces.api.member;

import com.taja.global.response.CommonApiResponse;
import com.taja.application.member.TokenDto;
import com.taja.application.member.AuthService;
import com.taja.application.member.CookieService;
import com.taja.interfaces.api.member.request.CheckDuplicateNameRequest;
import com.taja.interfaces.api.member.request.EmailRequest;
import com.taja.interfaces.api.member.request.LoginRequest;
import com.taja.interfaces.api.member.request.SignUpRequest;
import com.taja.interfaces.api.member.request.VerifyEmailRequest;
import com.taja.interfaces.api.member.response.CheckDuplicateNameResponse;
import com.taja.interfaces.api.member.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/auth")
@Tag(name = "Member", description = "Member API")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Operation(summary = "회원가입", description = "이름, 이메일, 비밀번호로 회원가입을 합니다.")
    @PostMapping("/signup")
    public CommonApiResponse<String> signup(@Valid @RequestBody SignUpRequest request) {
        authService.signup(request.name(), request.email(), request.password());
        return CommonApiResponse.success("회원가입에 성공했습니다.");
    }

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public CommonApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                                  HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request.email(), request.password());
        cookieService.addRefreshTokenCookie(response, tokenDto.refreshToken());

        TokenResponse tokenResponse = new TokenResponse(tokenDto.accessToken());
        return CommonApiResponse.success(tokenResponse, "로그인에 성공했습니다.");
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰 유효성 검사 후 액세스 토큰을 재발급합니다.")
    @PostMapping("/token")
    public CommonApiResponse<TokenResponse> reissueToken(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse newTokenResponse = authService.reissue(refreshToken);
        return CommonApiResponse.success(newTokenResponse, "새로운 액세스 토큰이 발급되었습니다.");
    }

    @Operation(summary = "이메일 인증 요청", description = "이메일로 인증 코드를 전송합니다.")
    @PostMapping("/email/send")
    public CommonApiResponse<String> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        authService.sendCodeToEmail(emailRequest.email());
        return CommonApiResponse.success("이메일 인증 요청을 성공했습니다.");
    }

    @Operation(summary = "이메일 인증 코드 검증", description = "이메일과 인증 코드를 검증합니다.")
    @PostMapping("/email/verify")
    public CommonApiResponse<String> validateEmailCode(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
        authService.verifyEmailCode(verifyEmailRequest.email(), verifyEmailRequest.code());
        return CommonApiResponse.success("이메일 인증을 성공했습니다.");
    }

    @Operation(summary = "이름 중복 확인", description = "이름이 중복되었는지 확인합니다.")
    @PostMapping("/name/duplicate-check")
    public CommonApiResponse<CheckDuplicateNameResponse> checkNameDuplicate(
            @Valid @RequestBody CheckDuplicateNameRequest checkDuplicateNameRequest) {
        boolean isDuplicate = authService.checkMemberNameDuplicate(checkDuplicateNameRequest.name());
        return CommonApiResponse.success(new CheckDuplicateNameResponse(isDuplicate), "이름 중복 확인을 성공했습니다.");
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 삭제하여 로그아웃합니다.")
    @PostMapping("/logout")
    public CommonApiResponse<String> logout(@CookieValue("refreshToken") String refreshToken) {
        authService.deleteRefreshToken(refreshToken);
        return CommonApiResponse.success("리프레시 토큰을 삭제했습니다.");
    }

}
