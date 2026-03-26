package com.taja.domain.station;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStation is a Querydsl query type for Station
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStation extends EntityPathBase<Station> {

    private static final long serialVersionUID = -1174929651L;

    public static final QStation station = new QStation("station");

    public final com.taja.global.QBaseEntity _super = new com.taja.global.QBaseEntity(this);

    public final StringPath address = createString("address");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath district = createString("district");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Integer> lcdHoldCount = createNumber("lcdHoldCount", Integer.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> number = createNumber("number", Integer.class);

    public final EnumPath<OperationMode> operationMode = createEnum("operationMode", OperationMode.class);

    public final NumberPath<Integer> qrHoldCount = createNumber("qrHoldCount", Integer.class);

    public final NumberPath<Long> stationId = createNumber("stationId", Long.class);

    public final NumberPath<Integer> totalHoldCount = createNumber("totalHoldCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStation(String variable) {
        super(Station.class, forVariable(variable));
    }

    public QStation(Path<? extends Station> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStation(PathMetadata metadata) {
        super(Station.class, metadata);
    }

}

