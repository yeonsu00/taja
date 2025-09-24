package com.taja.global;

import com.taja.jwt.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        System.out.println("test" + customUserDetails.getUsername());
        return "OK";
    }
}
