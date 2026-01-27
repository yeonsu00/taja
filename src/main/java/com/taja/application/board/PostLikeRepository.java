package com.taja.application.board;

import com.taja.domain.board.PostLike;
import java.util.List;

public interface PostLikeRepository {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);

    void saveAll(Iterable<PostLike> postLikes);
}
