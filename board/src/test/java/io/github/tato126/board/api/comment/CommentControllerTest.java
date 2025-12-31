package io.github.tato126.board.api.comment;

import io.github.tato126.board.api.comment.dto.CreateCommentRequest;
import io.github.tato126.board.api.comment.dto.UpdateCommentRequest;
import io.github.tato126.board.common.exception.GlobalExceptionHandler;
import io.github.tato126.board.common.exception.NotFoundException;
import io.github.tato126.board.domain.comment.Comment;
import io.github.tato126.board.domain.comment.CommentService;
import io.github.tato126.board.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private Post createPost() {
        Post post = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .author("게시글 작성자")
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    private Comment createComment(Long id, String content, String author, Post post) {
        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "replies", new ArrayList<>());
        return comment;
    }

    @Test
    @DisplayName("댓글 생성 API 테스트")
    void createComment() throws Exception {
        // given
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "테스트 댓글");
        ReflectionTestUtils.setField(request, "author", "댓글 작성자");

        Post post = createPost();
        Comment comment = createComment(1L, "테스트 댓글", "댓글 작성자", post);

        given(commentService.createComment(eq(postId), any(), any())).willReturn(comment);

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("테스트 댓글"))
                .andExpect(jsonPath("$.author").value("댓글 작성자"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 생성 시 404 응답")
    void createCommentPostNotFound() throws Exception {
        // given
        Long postId = 999L;
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "테스트 댓글");
        ReflectionTestUtils.setField(request, "author", "댓글 작성자");

        given(commentService.createComment(eq(postId), any(), any()))
                .willThrow(new NotFoundException("Post", postId));

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 생성 API - 유효성 검증 실패 테스트 (빈 내용)")
    void createCommentValidationFailEmptyContent() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "");
        ReflectionTestUtils.setField(request, "author", "작성자");

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 생성 API - 유효성 검증 실패 테스트 (빈 작성자)")
    void createCommentValidationFailEmptyAuthor() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "테스트 댓글");
        ReflectionTestUtils.setField(request, "author", "");

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 목록 조회 API 테스트")
    void getComments() throws Exception {
        // given
        Long postId = 1L;
        Post post = createPost();
        Comment comment1 = createComment(1L, "댓글1", "작성자1", post);
        Comment comment2 = createComment(2L, "댓글2", "작성자2", post);

        given(commentService.getComments(postId)).willReturn(List.of(comment1, comment2));

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("댓글1"))
                .andExpect(jsonPath("$[1].content").value("댓글2"));
    }

    @Test
    @DisplayName("대댓글 생성 API 테스트")
    void createReply() throws Exception {
        // given
        Long parentCommentId = 1L;
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "대댓글");
        ReflectionTestUtils.setField(request, "author", "대댓글 작성자");

        Post post = createPost();
        Comment parentComment = createComment(parentCommentId, "부모 댓글", "부모 작성자", post);

        Comment reply = Comment.builder()
                .content("대댓글")
                .author("대댓글 작성자")
                .post(post)
                .parent(parentComment)
                .build();
        ReflectionTestUtils.setField(reply, "id", 2L);
        ReflectionTestUtils.setField(reply, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(reply, "replies", new ArrayList<>());

        given(commentService.createReply(eq(parentCommentId), any(), any())).willReturn(reply);

        // when & then
        mockMvc.perform(post("/api/comments/{id}/replies", parentCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.content").value("대댓글"));
    }

    @Test
    @DisplayName("존재하지 않는 부모 댓글에 대댓글 생성 시 404 응답")
    void createReplyParentNotFound() throws Exception {
        // given
        Long parentCommentId = 999L;
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "대댓글");
        ReflectionTestUtils.setField(request, "author", "대댓글 작성자");

        given(commentService.createReply(eq(parentCommentId), any(), any()))
                .willThrow(new NotFoundException("Comment", parentCommentId));

        // when & then
        mockMvc.perform(post("/api/comments/{id}/replies", parentCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 수정 API 테스트")
    void updateComment() throws Exception {
        // given
        Long commentId = 1L;
        UpdateCommentRequest request = new UpdateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "수정된 댓글");

        Post post = createPost();
        Comment updatedComment = createComment(commentId, "수정된 댓글", "작성자", post);

        given(commentService.updateComment(eq(commentId), any())).willReturn(updatedComment);

        // when & then
        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글"));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 404 응답")
    void updateCommentNotFound() throws Exception {
        // given
        Long commentId = 999L;
        UpdateCommentRequest request = new UpdateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "수정된 댓글");

        given(commentService.updateComment(eq(commentId), any()))
                .willThrow(new NotFoundException("Comment", commentId));

        // when & then
        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 수정 API - 유효성 검증 실패 테스트 (빈 내용)")
    void updateCommentValidationFail() throws Exception {
        // given
        UpdateCommentRequest request = new UpdateCommentRequest();
        ReflectionTestUtils.setField(request, "content", "");

        // when & then
        mockMvc.perform(put("/api/comments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 삭제 API 테스트")
    void deleteComment() throws Exception {
        // given
        Long commentId = 1L;
        doNothing().when(commentService).deleteComment(commentId);

        // when & then
        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 404 응답")
    void deleteCommentNotFound() throws Exception {
        // given
        Long commentId = 999L;
        doThrow(new NotFoundException("Comment", commentId))
                .when(commentService).deleteComment(commentId);

        // when & then
        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isNotFound());
    }
}
