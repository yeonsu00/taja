package com.taja.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taja.global.response.CommonApiResponse;
import com.taja.global.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseCode responseCode = ResponseCode.TOKEN_ERROR;
        CommonApiResponse<?> body = CommonApiResponse.failure(responseCode, "액세스 토큰이 필요합니다.");

        response.setStatus(responseCode.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
