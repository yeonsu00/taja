package com.taja.application.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    public void softDeletePostLikes(Long postId) {
        List<PostLike> postLikes = postLikeRepository.findByPostIdAndIsDeletedFalse(postId);
        postLikes.forEach(PostLike::markAsDeleted);
    }

}
