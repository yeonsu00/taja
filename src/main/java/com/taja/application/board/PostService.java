package com.taja.application.board;

import com.taja.application.board.BoardInfo.PostItem;
import com.taja.domain.board.Post;
import com.taja.global.exception.PostNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public void createPost(Long memberId, Long stationId, String content) {
        Post post = Post.of(stationId, memberId, content);
        postRepository.savePost(post);
    }

    public List<BoardInfo.PostItem> findLatestPosts(Long stationId, String cursor, int size) {
        Long cursorPostId = PostCursor.decode(cursor);
        return postRepository.findLatestPosts(stationId, cursorPostId, size);
    }

    public BoardInfo.PostDetailPart findPostDetailPart(Long postId) {
        return postRepository.findPostDetailPartByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public BoardInfo.PostDetail enrichWithComments(BoardInfo.PostDetailPart part) {
        List<BoardInfo.CommentItem> comments = postRepository.findCommentItemsByPostId(part.postId());
        return new BoardInfo.PostDetail(
                part.postId(),
                part.writer(),
                part.createdAt(),
                part.content(),
                part.likeCount(),
                part.commentCount(),
                comments
        );
    }

    public boolean hasNext(List<PostItem> fetchedItems, int size) {
        return fetchedItems.size() > size;
    }

    public Post findPostByPostAndMember(Long memberId, Long postId) {
        return postRepository.findPostByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public void softDeletePost(Post post) {
        post.markAsDeleted();
    }

    public Post findPostByPostId(Long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public void incrementCommentCount(Long postId) {
        int updated = postRepository.increaseCommentCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void decrementCommentCount(Long postId) {
        int updated = postRepository.decreaseCommentCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void incrementLikeCount(Long postId) {
        int updated = postRepository.increaseLikeCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void decrementLikeCount(Long postId) {
        int updated = postRepository.decreaseLikeCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }
}
