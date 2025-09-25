package com.taja.global;

import com.taja.jwt.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("test {}", customUserDetails.getUsername());
        return "OK";
    }
}
