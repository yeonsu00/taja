package com.taja.infrastructure.board;

import com.taja.application.board.PostLikeRepository;
import com.taja.domain.board.PostLike;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public List<PostLike> findByPostIdAndIsDeletedFalse(Long postId) {
        return postLikeJpaRepository.findByPostIdAndIsDeletedFalse(postId);
    }

    @Override
    public void saveAll(Iterable<PostLike> postLikes) {
        postLikeJpaRepository.saveAll(postLikes);
    }
}
