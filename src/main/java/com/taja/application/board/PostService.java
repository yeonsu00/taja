package com.taja.application.board;

import com.taja.domain.board.Post;
import com.taja.global.exception.NotStationMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final BoardMemberRepository boardMemberRepository;
    private final PostRepository postRepository;

    public void createPost(Long memberId, Long stationId, String content) {
        if (!boardMemberRepository.existsByStationIdAndMemberId(stationId, memberId)) {
            throw new NotStationMemberException("해당 게시판의 참여자가 아닙니다.");
        }

        Post post = Post.of(stationId, memberId, content);
        postRepository.savePost(post);
    }
}
