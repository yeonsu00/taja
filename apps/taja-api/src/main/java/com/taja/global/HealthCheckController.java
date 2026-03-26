package com.taja.global;

import com.taja.infrastructure.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "healthcheck", description = "healthcheck API")
public class HealthCheckController {

    @Operation(summary = "health check", description = "서버 상태 확인")
    @GetMapping("/health")
    public String healthCheck(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("test {}", customUserDetails.getUsername());
        return "OK";
    }
}
