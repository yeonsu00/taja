package com.taja.weather.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DistrictPoint {

    private Long districtPointId;

    private String districtName;

    private Integer xPoint;

    private Integer yPoint;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    private DistrictPoint(Long districtPointId, String districtName, Integer xPoint, Integer yPoint,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.districtPointId = districtPointId;
        this.districtName = districtName;
        this.xPoint = xPoint;
        this.yPoint = yPoint;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DistrictPoint of(String districtName, Integer xPoint, Integer yPoint) {
        return DistrictPoint.builder()
                .districtName(districtName)
                .xPoint(xPoint)
                .yPoint(yPoint)
                .build();
    }
}
