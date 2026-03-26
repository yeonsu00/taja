package com.taja.domain.weather;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDistrictPoint is a Querydsl query type for DistrictPoint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDistrictPoint extends EntityPathBase<DistrictPoint> {

    private static final long serialVersionUID = -2047469445L;

    public static final QDistrictPoint districtPoint = new QDistrictPoint("districtPoint");

    public final com.taja.global.QBaseEntity _super = new com.taja.global.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath districtName = createString("districtName");

    public final NumberPath<Long> districtPointId = createNumber("districtPointId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> xPoint = createNumber("xPoint", Integer.class);

    public final NumberPath<Integer> yPoint = createNumber("yPoint", Integer.class);

    public QDistrictPoint(String variable) {
        super(DistrictPoint.class, forVariable(variable));
    }

    public QDistrictPoint(Path<? extends DistrictPoint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDistrictPoint(PathMetadata metadata) {
        super(DistrictPoint.class, metadata);
    }

}

