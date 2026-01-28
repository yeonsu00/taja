package com.taja.infrastructure.board;

import com.taja.domain.board.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);

    Optional<Post> findByPostIdAndWriterIdAndIsDeletedFalse(Long postId, Long writerId);
}
