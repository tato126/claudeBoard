package io.github.tato126.board.domain.comment;

import io.github.tato126.board.domain.post.Post;
import io.github.tato126.board.domain.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private Post savedPost;

    @BeforeEach
    void setUp() {
        savedPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author("게시글 작성자")
                .build());
    }

    @Test
    @DisplayName("댓글 저장 테스트")
    void saveComment() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .author("댓글 작성자")
                .post(savedPost)
                .build();

        // when
        Comment savedComment = commentRepository.save(comment);

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("테스트 댓글");
        assertThat(savedComment.getAuthor()).isEqualTo("댓글 작성자");
        assertThat(savedComment.getPost().getId()).isEqualTo(savedPost.getId());
    }

    @Test
    @DisplayName("댓글 ID로 조회 테스트")
    void findById() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .author("댓글 작성자")
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);

        // when
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("테스트 댓글");
    }

    @Test
    @DisplayName("게시글의 최상위 댓글만 조회 테스트")
    void findByPostIdAndParentIsNull() {
        // given
        Comment comment1 = Comment.builder()
                .content("최상위 댓글 1")
                .author("작성자1")
                .post(savedPost)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .content("최상위 댓글 2")
                .author("작성자2")
                .post(savedPost)
                .build();
        commentRepository.save(comment2);

        Comment reply = Comment.builder()
                .content("대댓글")
                .author("작성자3")
                .post(savedPost)
                .parent(comment1)
                .build();
        commentRepository.save(reply);

        // when
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentIsNull(savedPost.getId());

        // then
        assertThat(topLevelComments).hasSize(2);
        assertThat(topLevelComments).extracting("content")
                .containsExactlyInAnyOrder("최상위 댓글 1", "최상위 댓글 2");
    }

    @Test
    @DisplayName("대댓글 저장 테스트")
    void saveReply() {
        // given
        Comment parentComment = Comment.builder()
                .content("부모 댓글")
                .author("부모 작성자")
                .post(savedPost)
                .build();
        commentRepository.save(parentComment);

        Comment reply = Comment.builder()
                .content("대댓글")
                .author("대댓글 작성자")
                .post(savedPost)
                .parent(parentComment)
                .build();

        // when
        Comment savedReply = commentRepository.save(reply);

        // then
        assertThat(savedReply.getParent()).isNotNull();
        assertThat(savedReply.getParent().getId()).isEqualTo(parentComment.getId());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateComment() {
        // given
        Comment comment = Comment.builder()
                .content("원래 댓글")
                .author("작성자")
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);

        // when
        savedComment.updateContent("수정된 댓글");
        commentRepository.flush();

        // then
        Comment updatedComment = commentRepository.findById(savedComment.getId()).get();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteComment() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .author("작성자")
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);
        Long commentId = savedComment.getId();

        // when
        commentRepository.delete(savedComment);

        // then
        Optional<Comment> deletedComment = commentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();
    }

    @Test
    @DisplayName("게시글 삭제 시 댓글도 함께 삭제 (Cascade) 테스트")
    void deletePostWithComments() {
        // given
        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .author("작성자")
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);
        Long commentId = savedComment.getId();

        // when
        postRepository.delete(savedPost);
        postRepository.flush();

        // then
        Optional<Comment> deletedComment = commentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();
    }
}
