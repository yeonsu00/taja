package com.taja.simulator.interfaces.api.dto;

import java.util.List;

public record SimulationRequest(
        int durationSeconds,
        long delayMinMs,
        long delayMaxMs,
        boolean useAiContent,
        List<UserConfig> users
) {
    public record UserConfig(
            String personaName,
            String personaDescription,
            List<String> actions
    ) {}
}
