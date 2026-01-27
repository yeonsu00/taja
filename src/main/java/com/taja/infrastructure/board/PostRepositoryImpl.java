package com.taja.infrastructure.board;

import com.taja.application.board.PostRepository;
import com.taja.domain.board.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public void savePost(Post post) {
        postJpaRepository.save(post);
    }
}
