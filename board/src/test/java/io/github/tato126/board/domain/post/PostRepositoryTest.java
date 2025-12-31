package io.github.tato126.board.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 저장 테스트")
    void savePost() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .author("작성자")
                .build();

        // when
        Post savedPost = postRepository.save(post);

        // then
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(savedPost.getContent()).isEqualTo("테스트 내용");
        assertThat(savedPost.getAuthor()).isEqualTo("작성자");
    }

    @Test
    @DisplayName("게시글 ID로 조회 테스트")
    void findById() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .author("작성자")
                .build();
        Post savedPost = postRepository.save(post);

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 빈 Optional 반환")
    void findByIdNotFound() {
        // when
        Optional<Post> foundPost = postRepository.findById(999L);

        // then
        assertThat(foundPost).isEmpty();
    }

    @Test
    @DisplayName("게시글 페이징 조회 테스트")
    void findAllWithPaging() {
        // given
        for (int i = 1; i <= 15; i++) {
            Post post = Post.builder()
                    .title("제목 " + i)
                    .content("내용 " + i)
                    .author("작성자")
                    .build();
            postRepository.save(post);
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<Post> postPage = postRepository.findAll(pageable);

        // then
        assertThat(postPage.getContent()).hasSize(10);
        assertThat(postPage.getTotalElements()).isEqualTo(15);
        assertThat(postPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePost() {
        // given
        Post post = Post.builder()
                .title("원래 제목")
                .content("원래 내용")
                .author("작성자")
                .build();
        Post savedPost = postRepository.save(post);

        // when
        savedPost.update("수정된 제목", "수정된 내용");
        postRepository.flush();

        // then
        Post updatedPost = postRepository.findById(savedPost.getId()).get();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .author("작성자")
                .build();
        Post savedPost = postRepository.save(post);
        Long postId = savedPost.getId();

        // when
        postRepository.delete(savedPost);

        // then
        Optional<Post> deletedPost = postRepository.findById(postId);
        assertThat(deletedPost).isEmpty();
    }
}
