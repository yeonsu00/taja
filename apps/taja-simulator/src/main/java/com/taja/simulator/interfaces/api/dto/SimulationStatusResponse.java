package com.taja.simulator.interfaces.api.dto;

public record SimulationStatusResponse(
        boolean running,
        int totalUsers,
        int activeUsers,
        int completedActions,
        int successCount,
        int failureCount
) {
}
