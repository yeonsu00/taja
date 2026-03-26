package com.taja.domain.status;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStationStatus is a Querydsl query type for StationStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStationStatus extends EntityPathBase<StationStatus> {

    private static final long serialVersionUID = -462544809L;

    public static final QStationStatus stationStatus = new QStationStatus("stationStatus");

    public final NumberPath<Integer> parkingBikeCount = createNumber("parkingBikeCount", Integer.class);

    public final DatePath<java.time.LocalDate> requestedDate = createDate("requestedDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> requestedTime = createTime("requestedTime", java.time.LocalTime.class);

    public final NumberPath<Integer> stationNumber = createNumber("stationNumber", Integer.class);

    public final NumberPath<Long> stationStatusId = createNumber("stationStatusId", Long.class);

    public QStationStatus(String variable) {
        super(StationStatus.class, forVariable(variable));
    }

    public QStationStatus(Path<? extends StationStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStationStatus(PathMetadata metadata) {
        super(StationStatus.class, metadata);
    }

}

