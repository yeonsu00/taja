package com.taja.domain.statistics;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStatisticsBase is a Querydsl query type for StatisticsBase
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QStatisticsBase extends EntityPathBase<StatisticsBase> {

    private static final long serialVersionUID = -1010561070L;

    public static final QStatisticsBase statisticsBase = new QStatisticsBase("statisticsBase");

    public final com.taja.global.QBaseEntity _super = new com.taja.global.QBaseEntity(this);

    public final NumberPath<Integer> avgParkingBikeCount = createNumber("avgParkingBikeCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> sampleCount = createNumber("sampleCount", Long.class);

    public final NumberPath<Long> stationId = createNumber("stationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStatisticsBase(String variable) {
        super(StatisticsBase.class, forVariable(variable));
    }

    public QStatisticsBase(Path<? extends StatisticsBase> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatisticsBase(PathMetadata metadata) {
        super(StatisticsBase.class, metadata);
    }

}

