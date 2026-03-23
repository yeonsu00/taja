package com.taja.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailCode is a Querydsl query type for EmailCode
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailCode extends EntityPathBase<EmailCode> {

    private static final long serialVersionUID = 1435407074L;

    public static final QEmailCode emailCode = new QEmailCode("emailCode");

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> emailCodeId = createNumber("emailCodeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public QEmailCode(String variable) {
        super(EmailCode.class, forVariable(variable));
    }

    public QEmailCode(Path<? extends EmailCode> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailCode(PathMetadata metadata) {
        super(EmailCode.class, metadata);
    }

}

