package com.taja.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taja.global.exception.TokenException;
import com.taja.global.response.CommonApiResponse;
import com.taja.global.response.ResponseCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (TokenException ex) {
            setErrorResponse(response, ex);
        }
    }

    private void setErrorResponse(HttpServletResponse response, TokenException ex) throws IOException {
        ResponseCode responseCode = ResponseCode.TOKEN_ERROR;
        CommonApiResponse<?> body = CommonApiResponse.failure(responseCode, ex.getMessage());

        response.setStatus(responseCode.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
