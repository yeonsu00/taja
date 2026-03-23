package com.taja.domain.status;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStationStatusHourlyAvg is a Querydsl query type for StationStatusHourlyAvg
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStationStatusHourlyAvg extends EntityPathBase<StationStatusHourlyAvg> {

    private static final long serialVersionUID = 507017226L;

    public static final QStationStatusHourlyAvg stationStatusHourlyAvg = new QStationStatusHourlyAvg("stationStatusHourlyAvg");

    public final NumberPath<Integer> avgParkingBikeCount = createNumber("avgParkingBikeCount", Integer.class);

    public final DatePath<java.time.LocalDate> baseDate = createDate("baseDate", java.time.LocalDate.class);

    public final NumberPath<Integer> baseHour = createNumber("baseHour", Integer.class);

    public final NumberPath<Long> sampleCount = createNumber("sampleCount", Long.class);

    public final NumberPath<Integer> stationNumber = createNumber("stationNumber", Integer.class);

    public final NumberPath<Long> stationStatusHourlyAvgId = createNumber("stationStatusHourlyAvgId", Long.class);

    public QStationStatusHourlyAvg(String variable) {
        super(StationStatusHourlyAvg.class, forVariable(variable));
    }

    public QStationStatusHourlyAvg(Path<? extends StationStatusHourlyAvg> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStationStatusHourlyAvg(PathMetadata metadata) {
        super(StationStatusHourlyAvg.class, metadata);
    }

}

