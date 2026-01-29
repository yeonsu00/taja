package com.taja.application.board;

import com.taja.domain.board.PostLike;
import com.taja.global.exception.AlreadyLikedException;
import com.taja.global.exception.LikeNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    public void likePost(Long memberId, Long postId) {
        if (postLikeRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId)) {
            throw new AlreadyLikedException("이미 좋아요를 누른 게시글입니다.");
        }

        PostLike like = PostLike.of(postId, memberId);
        postLikeRepository.savePostLike(like);
    }

    public void unlikePost(Long memberId, Long postId) {
        PostLike like = postLikeRepository.findByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId)
                .orElseThrow(() -> new LikeNotFoundException("해당 게시글에 좋아요를 누른 사용자가 아닙니다."));

        like.markAsDeleted();
        postLikeRepository.savePostLike(like);
    }

    public void softDeletePostLikes(Long postId) {
        List<PostLike> postLikes = postLikeRepository.findByPostIdAndIsDeletedFalse(postId);
        postLikes.forEach(PostLike::markAsDeleted);
    }
}
