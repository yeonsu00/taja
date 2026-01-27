package com.taja.infrastructure.board;

import com.taja.application.board.BoardInfo;
import com.taja.application.board.PostRepository;
import com.taja.domain.board.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;
    private final PostQueryRepository postQueryRepository;

    @Override
    public void savePost(Post post) {
        postJpaRepository.save(post);
    }

    @Override
    public List<BoardInfo.PostItem> findLatestPosts(Long stationId, Long cursor, int size) {
        return postQueryRepository.findLatestPosts(stationId, cursor, size);
    }
}
