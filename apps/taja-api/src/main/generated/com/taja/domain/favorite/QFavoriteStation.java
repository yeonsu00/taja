package com.taja.domain.favorite;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFavoriteStation is a Querydsl query type for FavoriteStation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoriteStation extends EntityPathBase<FavoriteStation> {

    private static final long serialVersionUID = 1007747123L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFavoriteStation favoriteStation = new QFavoriteStation("favoriteStation");

    public final NumberPath<Long> favoriteStationId = createNumber("favoriteStationId", Long.class);

    public final com.taja.domain.member.QMember member;

    public final com.taja.domain.station.QStation station;

    public QFavoriteStation(String variable) {
        this(FavoriteStation.class, forVariable(variable), INITS);
    }

    public QFavoriteStation(Path<? extends FavoriteStation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFavoriteStation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFavoriteStation(PathMetadata metadata, PathInits inits) {
        this(FavoriteStation.class, metadata, inits);
    }

    public QFavoriteStation(Class<? extends FavoriteStation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.taja.domain.member.QMember(forProperty("member")) : null;
        this.station = inits.isInitialized("station") ? new com.taja.domain.station.QStation(forProperty("station")) : null;
    }

}

