package com.taja.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBoardMember is a Querydsl query type for BoardMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardMember extends EntityPathBase<BoardMember> {

    private static final long serialVersionUID = 262974955L;

    public static final QBoardMember boardMember = new QBoardMember("boardMember");

    public final com.taja.global.QBaseEntity _super = new com.taja.global.QBaseEntity(this);

    public final NumberPath<Long> boardMemberId = createNumber("boardMemberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> stationId = createNumber("stationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBoardMember(String variable) {
        super(BoardMember.class, forVariable(variable));
    }

    public QBoardMember(Path<? extends BoardMember> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoardMember(PathMetadata metadata) {
        super(BoardMember.class, metadata);
    }

}

