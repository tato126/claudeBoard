package io.github.tato126.board.api.post;

import io.github.tato126.board.api.post.dto.CreatePostRequest;
import io.github.tato126.board.api.post.dto.PostListResponse;
import io.github.tato126.board.api.post.dto.PostResponse;
import io.github.tato126.board.api.post.dto.UpdatePostRequest;
import io.github.tato126.board.domain.post.Post;
import io.github.tato126.board.domain.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post post = postService.createPost(
                request.getTitle(),
                request.getContent(),
                request.getAuthor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPosts(Pageable pageable) {
        Page<PostListResponse> posts = postService.getPosts(pageable)
                .map(PostListResponse::from);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        Post post = postService.getPost(id);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        Post post = postService.updatePost(id, request.getTitle(), request.getContent());
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
