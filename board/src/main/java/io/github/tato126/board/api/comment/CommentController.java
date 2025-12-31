package io.github.tato126.board.api.comment;

import io.github.tato126.board.api.comment.dto.CommentResponse;
import io.github.tato126.board.api.comment.dto.CreateCommentRequest;
import io.github.tato126.board.api.comment.dto.UpdateCommentRequest;
import io.github.tato126.board.domain.comment.Comment;
import io.github.tato126.board.domain.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Comment comment = commentService.createComment(
                postId,
                request.getContent(),
                request.getAuthor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getComments(postId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/api/comments/{id}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Comment reply = commentService.createReply(
                id,
                request.getContent(),
                request.getAuthor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(reply));
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        Comment comment = commentService.updateComment(id, request.getContent());
        return ResponseEntity.ok(CommentResponse.from(comment));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
