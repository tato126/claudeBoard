package io.github.tato126.board.api.post;

import io.github.tato126.board.api.post.dto.CreatePostRequest;
import io.github.tato126.board.api.post.dto.UpdatePostRequest;
import io.github.tato126.board.common.exception.GlobalExceptionHandler;
import io.github.tato126.board.common.exception.NotFoundException;
import io.github.tato126.board.domain.post.Post;
import io.github.tato126.board.domain.post.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(GlobalExceptionHandler.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    private Post createPost(Long id, String title, String content, String author) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now());
        return post;
    }

    @Test
    @DisplayName("게시글 생성 API 테스트")
    void createPost() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest();
        ReflectionTestUtils.setField(request, "title", "테스트 제목");
        ReflectionTestUtils.setField(request, "content", "테스트 내용");
        ReflectionTestUtils.setField(request, "author", "작성자");

        Post post = createPost(1L, "테스트 제목", "테스트 내용", "작성자");
        given(postService.createPost(any(), any(), any())).willReturn(post);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.author").value("작성자"));
    }

    @Test
    @DisplayName("게시글 생성 API - 유효성 검증 실패 테스트 (빈 제목)")
    void createPostValidationFailEmptyTitle() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest();
        ReflectionTestUtils.setField(request, "title", "");
        ReflectionTestUtils.setField(request, "content", "테스트 내용");
        ReflectionTestUtils.setField(request, "author", "작성자");

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 생성 API - 유효성 검증 실패 테스트 (빈 내용)")
    void createPostValidationFailEmptyContent() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest();
        ReflectionTestUtils.setField(request, "title", "테스트 제목");
        ReflectionTestUtils.setField(request, "content", "");
        ReflectionTestUtils.setField(request, "author", "작성자");

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 생성 API - 유효성 검증 실패 테스트 (빈 작성자)")
    void createPostValidationFailEmptyAuthor() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest();
        ReflectionTestUtils.setField(request, "title", "테스트 제목");
        ReflectionTestUtils.setField(request, "content", "테스트 내용");
        ReflectionTestUtils.setField(request, "author", "");

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 목록 조회 API 테스트")
    void getPosts() throws Exception {
        // given
        Post post1 = createPost(1L, "제목1", "내용1", "작성자1");
        Post post2 = createPost(2L, "제목2", "내용2", "작성자2");
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), PageRequest.of(0, 10), 2);

        given(postService.getPosts(any())).willReturn(postPage);

        // when & then
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("게시글 단건 조회 API 테스트")
    void getPost() throws Exception {
        // given
        Post post = createPost(1L, "테스트 제목", "테스트 내용", "작성자");
        given(postService.getPost(1L)).willReturn(post);

        // when & then
        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 제목"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 응답")
    void getPostNotFound() throws Exception {
        // given
        given(postService.getPost(999L)).willThrow(new NotFoundException("Post", 999L));

        // when & then
        mockMvc.perform(get("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 수정 API 테스트")
    void updatePost() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest();
        ReflectionTestUtils.setField(request, "title", "수정된 제목");
        ReflectionTestUtils.setField(request, "content", "수정된 내용");

        Post updatedPost = createPost(1L, "수정된 제목", "수정된 내용", "작성자");
        given(postService.updatePost(eq(1L), any(), any())).willReturn(updatedPost);

        // when & then
        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 404 응답")
    void updatePostNotFound() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest();
        ReflectionTestUtils.setField(request, "title", "수정된 제목");
        ReflectionTestUtils.setField(request, "content", "수정된 내용");

        given(postService.updatePost(eq(999L), any(), any()))
                .willThrow(new NotFoundException("Post", 999L));

        // when & then
        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 삭제 API 테스트")
    void deletePost() throws Exception {
        // given
        doNothing().when(postService).deletePost(1L);

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 404 응답")
    void deletePostNotFound() throws Exception {
        // given
        doThrow(new NotFoundException("Post", 999L)).when(postService).deletePost(999L);

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
