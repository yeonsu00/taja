package com.taja.interfaces.api.admin;

import com.taja.application.admin.SimulationDataCleanupFacade;
import com.taja.global.response.CommonApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/simulation")
@RequiredArgsConstructor
public class SimulationDataCleanupController {

    private final SimulationDataCleanupFacade cleanupService;

    @DeleteMapping("/data")
    public CommonApiResponse<String> cleanupSimulationData() {
        int count = cleanupService.cleanupSimulationData();
        return CommonApiResponse.success(count + "명의 시뮬레이션 사용자 데이터를 삭제했습니다.");
    }
}
