package com.taja.domain.statistics;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDayOfWeekStatistics is a Querydsl query type for DayOfWeekStatistics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDayOfWeekStatistics extends EntityPathBase<DayOfWeekStatistics> {

    private static final long serialVersionUID = -1021273300L;

    public static final QDayOfWeekStatistics dayOfWeekStatistics = new QDayOfWeekStatistics("dayOfWeekStatistics");

    public final QStatisticsBase _super = new QStatisticsBase(this);

    //inherited
    public final NumberPath<Integer> avgParkingBikeCount = _super.avgParkingBikeCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<java.time.DayOfWeek> dayOfWeek = createEnum("dayOfWeek", java.time.DayOfWeek.class);

    public final NumberPath<Long> dayOfWeekStatisticsId = createNumber("dayOfWeekStatisticsId", Long.class);

    //inherited
    public final NumberPath<Long> sampleCount = _super.sampleCount;

    //inherited
    public final NumberPath<Long> stationId = _super.stationId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDayOfWeekStatistics(String variable) {
        super(DayOfWeekStatistics.class, forVariable(variable));
    }

    public QDayOfWeekStatistics(Path<? extends DayOfWeekStatistics> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDayOfWeekStatistics(PathMetadata metadata) {
        super(DayOfWeekStatistics.class, metadata);
    }

}

