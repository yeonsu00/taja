package com.taja.weather.infra;

import com.taja.global.BaseEntity;
import com.taja.weather.domain.DistrictPoint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "district_point")
@RequiredArgsConstructor
public class DistrictPointEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long districtPointId;

    @Column(nullable = false, unique = true)
    private String districtName;

    @Column(nullable = false)
    private Integer xPoint;

    @Column(nullable = false)
    private Integer yPoint;

    @Builder
    private DistrictPointEntity(Long districtPointId, String districtName, Integer xPoint, Integer yPoint) {
        this.districtPointId = districtPointId;
        this.districtName = districtName;
        this.xPoint = xPoint;
        this.yPoint = yPoint;
    }

    public static DistrictPointEntity fromDistrictPoint(DistrictPoint districtPoint) {
        return DistrictPointEntity.builder()
                .districtName(districtPoint.getDistrictName())
                .xPoint(districtPoint.getXPoint())
                .yPoint(districtPoint.getYPoint())
                .build();
    }

    public void updatePoint(Integer xPoint, Integer yPoint) {
        this.xPoint = xPoint;
        this.yPoint = yPoint;
    }
}
