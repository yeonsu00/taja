package com.taja.domain.weather;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "district_point")
@RequiredArgsConstructor
@Getter
public class DistrictPoint extends BaseEntity {

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
    private DistrictPoint(Long districtPointId, String districtName, Integer xPoint, Integer yPoint) {
        this.districtPointId = districtPointId;
        this.districtName = districtName;
        this.xPoint = xPoint;
        this.yPoint = yPoint;
    }

    public static DistrictPoint of(String districtName, Integer xPoint, Integer yPoint) {
        return DistrictPoint.builder()
                .districtName(districtName)
                .xPoint(xPoint)
                .yPoint(yPoint)
                .build();
    }

    public void updatePoint(Integer xPoint, Integer yPoint) {
        this.xPoint = xPoint;
        this.yPoint = yPoint;
    }
}
