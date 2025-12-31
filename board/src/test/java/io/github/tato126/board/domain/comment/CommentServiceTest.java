package io.github.tato126.board.domain.comment;

import io.github.tato126.board.common.exception.NotFoundException;
import io.github.tato126.board.domain.post.Post;
import io.github.tato126.board.domain.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    private Post createPost() {
        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author("게시글 작성자")
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    @Test
    @DisplayName("댓글 생성 테스트")
    void createComment() {
        // given
        Long postId = 1L;
        Post post = createPost();
        String content = "테스트 댓글";
        String author = "댓글 작성자";

        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        Comment createdComment = commentService.createComment(postId, content, author);

        // then
        assertThat(createdComment.getId()).isEqualTo(1L);
        assertThat(createdComment.getContent()).isEqualTo(content);
        assertThat(createdComment.getAuthor()).isEqualTo(author);
        verify(postRepository).findById(postId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 생성 시 예외 발생")
    void createCommentPostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, "내용", "작성자"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("대댓글 생성 테스트")
    void createReply() {
        // given
        Long parentCommentId = 1L;
        Post post = createPost();

        Comment parentComment = Comment.builder()
                .content("부모 댓글")
                .author("부모 작성자")
                .post(post)
                .build();
        ReflectionTestUtils.setField(parentComment, "id", parentCommentId);

        Comment reply = Comment.builder()
                .content("대댓글")
                .author("대댓글 작성자")
                .post(post)
                .parent(parentComment)
                .build();
        ReflectionTestUtils.setField(reply, "id", 2L);

        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willReturn(reply);

        // when
        Comment createdReply = commentService.createReply(parentCommentId, "대댓글", "대댓글 작성자");

        // then
        assertThat(createdReply.getId()).isEqualTo(2L);
        assertThat(createdReply.getContent()).isEqualTo("대댓글");
        assertThat(createdReply.getParent()).isNotNull();
        verify(commentRepository).findById(parentCommentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 부모 댓글에 대댓글 생성 시 예외 발생")
    void createReplyParentNotFound() {
        // given
        Long parentCommentId = 999L;
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createReply(parentCommentId, "내용", "작성자"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("게시글의 최상위 댓글 목록 조회 테스트")
    void getComments() {
        // given
        Long postId = 1L;
        Post post = createPost();

        Comment comment1 = Comment.builder()
                .content("댓글 1")
                .author("작성자1")
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment1, "id", 1L);

        Comment comment2 = Comment.builder()
                .content("댓글 2")
                .author("작성자2")
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment2, "id", 2L);

        given(commentRepository.findByPostIdAndParentIsNull(postId))
                .willReturn(List.of(comment1, comment2));

        // when
        List<Comment> comments = commentService.getComments(postId);

        // then
        assertThat(comments).hasSize(2);
        verify(commentRepository).findByPostIdAndParentIsNull(postId);
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateComment() {
        // given
        Long commentId = 1L;
        Post post = createPost();

        Comment comment = Comment.builder()
                .content("원래 내용")
                .author("작성자")
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        Comment updatedComment = commentService.updateComment(commentId, "수정된 내용");

        // then
        assertThat(updatedComment.getContent()).isEqualTo("수정된 내용");
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
    void updateCommentNotFound() {
        // given
        Long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, "내용"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteComment() {
        // given
        Long commentId = 1L;
        Post post = createPost();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .author("작성자")
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId);

        // then
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
    void deleteCommentNotFound() {
        // given
        Long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId))
                .isInstanceOf(NotFoundException.class);
    }
}
