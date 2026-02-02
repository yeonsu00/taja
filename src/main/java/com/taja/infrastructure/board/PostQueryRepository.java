package com.taja.infrastructure.board;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.taja.application.board.BoardInfo;
import com.taja.domain.board.QComment;
import com.taja.domain.board.QPost;
import com.taja.domain.member.QMember;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPost post = QPost.post;
    private static final QComment comment = QComment.comment;
    private static final QMember member = QMember.member;

    public List<BoardInfo.PostItem> findRecentPosts(Long stationId, int recentPostsSize) {
        return queryFactory
                .select(
                        Projections.constructor(BoardInfo.PostItem.class,
                                post.stationId,
                                post.postId,
                                member.name,
                                post.createdAt,
                                post.content,
                                post.commentCount,
                                post.likeCount,
                                Expressions.constant(false)
                        )
                )
                .from(post)
                .join(member).on(post.writerId.eq(member.memberId))
                .where(
                        post.stationId.eq(stationId),
                        post.isDeleted.isFalse()
                )
                .orderBy(post.createdAt.desc(), post.postId.desc())
                .limit(recentPostsSize)
                .fetch();
    }

    public List<BoardInfo.PostItem> findLatestPosts(Long stationId, long cursor, int size) {
        return queryFactory
                .select(
                        Projections.constructor(BoardInfo.PostItem.class,
                                post.stationId,
                                post.postId,
                                member.name,
                                post.createdAt,
                                post.content,
                                post.commentCount,
                                post.likeCount,
                                Expressions.constant(false)
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

    public Optional<String> findLatestPostContentByStationId(Long stationId) {
        return Optional.ofNullable(
                queryFactory
                        .select(post.content)
                        .from(post)
                        .where(
                                post.stationId.eq(stationId),
                                post.isDeleted.isFalse()
                        )
                        .orderBy(post.postId.desc())
                        .limit(1)
                        .fetchFirst()
        );
    }

    public Optional<BoardInfo.PostDetailPart> findPostDetailPartByPostId(Long postId) {
        List<BoardInfo.PostDetailPart> list = queryFactory
                .select(
                        Projections.constructor(BoardInfo.PostDetailPart.class,
                                post.postId,
                                post.stationId,
                                member.name,
                                post.createdAt,
                                post.content,
                                post.likeCount,
                                post.commentCount
                        )
                )
                .from(post)
                .join(member).on(post.writerId.eq(member.memberId))
                .where(
                        post.postId.eq(postId),
                        post.isDeleted.isFalse()
                )
                .fetch();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    public List<BoardInfo.CommentItem> findCommentItemsByPostId(Long postId) {
        return queryFactory
                .select(
                        Projections.constructor(BoardInfo.CommentItem.class,
                                comment.commentId,
                                member.name,
                                comment.content,
                                comment.createdAt
                        )
                )
                .from(comment)
                .join(member).on(comment.writerId.eq(member.memberId))
                .where(
                        comment.postId.eq(postId),
                        comment.isDeleted.isFalse()
                )
                .orderBy(comment.createdAt.asc())
                .fetch();
    }

    public List<BoardInfo.PostItem> findPostItemsByPostIds(Long stationId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        List<BoardInfo.PostItem> fetched = queryFactory
                .select(
                        Projections.constructor(BoardInfo.PostItem.class,
                                post.stationId,
                                post.postId,
                                member.name,
                                post.createdAt,
                                post.content,
                                post.commentCount,
                                post.likeCount,
                                Expressions.constant(false)
                        )
                )
                .from(post)
                .join(member).on(post.writerId.eq(member.memberId))
                .where(
                        post.stationId.eq(stationId),
                        post.isDeleted.isFalse(),
                        post.postId.in(postIds)
                )
                .fetch();
        Map<Long, BoardInfo.PostItem> byId = new LinkedHashMap<>();
        for (BoardInfo.PostItem item : fetched) {
            byId.put(item.postId(), item);
        }
        List<BoardInfo.PostItem> ordered = new ArrayList<>(postIds.size());
        for (Long id : postIds) {
            BoardInfo.PostItem item = byId.get(id);
            if (item != null) {
                ordered.add(item);
            }
        }
        return ordered;
    }

    private BooleanExpression ltPostId(long cursor) {
        if (cursor <= 0) {
            return null;
        }
        return post.postId.lt(cursor);
    }
}
