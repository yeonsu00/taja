package com.taja.simulator.interfaces.api;

import com.taja.simulator.interfaces.api.dto.SimulationRequest;
import com.taja.simulator.interfaces.api.dto.SimulationStatusResponse;
import com.taja.simulator.application.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody SimulationRequest request) {
        simulationService.start(request);
        return ResponseEntity.ok("시뮬레이션이 시작되었습니다.");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        simulationService.stop();
        return ResponseEntity.ok("시뮬레이션이 중지되었습니다.");
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationStatusResponse> status() {
        return ResponseEntity.ok(simulationService.getStatus());
    }

    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        return simulationService.registerLogEmitter();
    }

    @DeleteMapping("/data")
    public ResponseEntity<String> cleanupData() {
        boolean ok = simulationService.cleanupSimulationData();
        if (ok) return ResponseEntity.ok("시뮬레이션 데이터가 삭제되었습니다.");
        return ResponseEntity.internalServerError().body("시뮬레이션 데이터 삭제 실패");
    }
}
