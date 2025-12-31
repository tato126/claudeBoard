package io.github.tato126.board.domain.post;

import io.github.tato126.board.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글 생성 테스트")
    void createPost() {
        // given
        String title = "테스트 제목";
        String content = "테스트 내용";
        String author = "작성자";

        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        Post createdPost = postService.createPost(title, content, author);

        // then
        assertThat(createdPost.getId()).isEqualTo(1L);
        assertThat(createdPost.getTitle()).isEqualTo(title);
        assertThat(createdPost.getContent()).isEqualTo(content);
        assertThat(createdPost.getAuthor()).isEqualTo(author);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록 페이징 조회 테스트")
    void getPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Post post1 = Post.builder().title("제목1").content("내용1").author("작성자1").build();
        Post post2 = Post.builder().title("제목2").content("내용2").author("작성자2").build();
        ReflectionTestUtils.setField(post1, "id", 1L);
        ReflectionTestUtils.setField(post2, "id", 2L);

        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), pageable, 2);
        given(postRepository.findAll(pageable)).willReturn(postPage);

        // when
        Page<Post> result = postService.getPosts(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(postRepository).findAll(pageable);
    }

    @Test
    @DisplayName("게시글 단건 조회 테스트")
    void getPost() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .author("작성자")
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        Post foundPost = postService.getPost(postId);

        // then
        assertThat(foundPost.getId()).isEqualTo(postId);
        assertThat(foundPost.getTitle()).isEqualTo("테스트 제목");
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
    void getPostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(NotFoundException.class);
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePost() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .title("원래 제목")
                .content("원래 내용")
                .author("작성자")
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        Post updatedPost = postService.updatePost(postId, "수정된 제목", "수정된 내용");

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    void updatePostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, "제목", "내용"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePost() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .author("작성자")
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postService.deletePost(postId);

        // then
        verify(postRepository).findById(postId);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    void deletePostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(postId))
                .isInstanceOf(NotFoundException.class);
    }
}
