package com.taja.infrastructure.board;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.taja.application.board.BoardInfo;
import com.taja.domain.board.QPost;
import com.taja.domain.member.QMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPost post = QPost.post;
    private static final QMember member = QMember.member;

    public List<BoardInfo.PostItem> findLatestPosts(Long stationId, Long cursor, int size) {
        return queryFactory
                .select(
                        Projections.constructor(BoardInfo.PostItem.class,
                                post.stationId,
                                post.postId,
                                member.name,
                                post.createdAt,
                                post.content,
                                post.commentCount,
                                post.likeCount
                        )
                )
                .from(post)
                .join(member).on(post.writerId.eq(member.memberId))
                .where(
                        post.stationId.eq(stationId),
                        post.isDeleted.isFalse(),
                        ltPostId(cursor)
                )
                .orderBy(post.postId.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression ltPostId(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return post.postId.lt(cursor);
    }
}
