package com.taja.application.board;

import com.taja.domain.board.Post;
import java.util.List;

public interface PostRepository {

    void savePost(Post post);

    List<BoardInfo.PostItem> findLatestPosts(Long stationId, Long cursor, int size);
}
