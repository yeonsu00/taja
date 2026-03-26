package com.taja.infrastructure.client.bike;

import com.taja.infrastructure.client.bike.dto.station.StationApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "bikeApiStationFeignClient",
        url = "${api.bike.base-url}${api.bike.key}${api.bike.station.path}"
)
public interface SeoulDataStationInfoFeignClient {

    @GetMapping("{startIndex}/{endIndex}")
    StationApiResponseDto getTbCycleStationInfo(
            @PathVariable("startIndex") int startIndex,
            @PathVariable("endIndex") int endIndex
    );
}
