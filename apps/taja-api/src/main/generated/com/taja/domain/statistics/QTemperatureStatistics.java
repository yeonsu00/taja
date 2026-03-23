package com.taja.domain.statistics;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTemperatureStatistics is a Querydsl query type for TemperatureStatistics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTemperatureStatistics extends EntityPathBase<TemperatureStatistics> {

    private static final long serialVersionUID = -193256775L;

    public static final QTemperatureStatistics temperatureStatistics = new QTemperatureStatistics("temperatureStatistics");

    public final QStatisticsBase _super = new QStatisticsBase(this);

    //inherited
    public final NumberPath<Integer> avgParkingBikeCount = _super.avgParkingBikeCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> sampleCount = _super.sampleCount;

    //inherited
    public final NumberPath<Long> stationId = _super.stationId;

    public final NumberPath<Double> temperatureRange = createNumber("temperatureRange", Double.class);

    public final NumberPath<Long> temperatureStatisticsId = createNumber("temperatureStatisticsId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTemperatureStatistics(String variable) {
        super(TemperatureStatistics.class, forVariable(variable));
    }

    public QTemperatureStatistics(Path<? extends TemperatureStatistics> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTemperatureStatistics(PathMetadata metadata) {
        super(TemperatureStatistics.class, metadata);
    }

}

