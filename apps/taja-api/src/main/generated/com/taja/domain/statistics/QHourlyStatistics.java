package com.taja.domain.statistics;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHourlyStatistics is a Querydsl query type for HourlyStatistics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHourlyStatistics extends EntityPathBase<HourlyStatistics> {

    private static final long serialVersionUID = 1465443378L;

    public static final QHourlyStatistics hourlyStatistics = new QHourlyStatistics("hourlyStatistics");

    public final QStatisticsBase _super = new QStatisticsBase(this);

    //inherited
    public final NumberPath<Integer> avgParkingBikeCount = _super.avgParkingBikeCount;

    public final NumberPath<Integer> baseHour = createNumber("baseHour", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> hourlyStatisticsId = createNumber("hourlyStatisticsId", Long.class);

    //inherited
    public final NumberPath<Long> sampleCount = _super.sampleCount;

    //inherited
    public final NumberPath<Long> stationId = _super.stationId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHourlyStatistics(String variable) {
        super(HourlyStatistics.class, forVariable(variable));
    }

    public QHourlyStatistics(Path<? extends HourlyStatistics> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHourlyStatistics(PathMetadata metadata) {
        super(HourlyStatistics.class, metadata);
    }

}

