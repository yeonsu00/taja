package com.taja.infrastructure.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, Long> {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);
}
