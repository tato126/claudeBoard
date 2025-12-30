# 도메인 모델 설계

## 개요
게시판 시스템의 도메인 모델 및 패키지 구조 설계

## 패키지 구조

```
io.github.tato126.board
├── domain
│   ├── post
│   │   ├── Post.java           # 게시글 엔티티
│   │   ├── PostRepository.java # 게시글 리포지토리
│   │   └── PostService.java    # 게시글 서비스
│   └── comment
│       ├── Comment.java        # 댓글 엔티티
│       ├── CommentRepository.java
│       └── CommentService.java
├── api
│   ├── post
│   │   ├── PostController.java
│   │   ├── PostRequest.java    # 요청 DTO
│   │   └── PostResponse.java   # 응답 DTO
│   └── comment
│       ├── CommentController.java
│       ├── CommentRequest.java
│       └── CommentResponse.java
├── common
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   └── NotFoundException.java
│   └── response
│       └── ErrorResponse.java
└── BoardApplication.java
```

## 도메인 모델

### Post (게시글)

```java
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String author;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
}
```

### Comment (댓글/대댓글)

```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;  // null이면 댓글, 값이 있으면 대댓글

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String author;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isReply() {
        return parent != null;
    }
}
```

## DTO 설계

### Request DTOs

```java
// 게시글 생성/수정 요청
public record PostRequest(
    @NotBlank String title,
    @NotBlank String content,
    @NotBlank String author
) {}

// 댓글 생성 요청
public record CommentRequest(
    @NotBlank String content,
    @NotBlank String author
) {}
```

### Response DTOs

```java
// 게시글 목록 응답
public record PostListResponse(
    Long id,
    String title,
    String author,
    LocalDateTime createdAt,
    int commentCount
) {}

// 게시글 상세 응답
public record PostDetailResponse(
    Long id,
    String title,
    String content,
    String author,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CommentResponse> comments
) {}

// 댓글 응답
public record CommentResponse(
    Long id,
    String content,
    String author,
    LocalDateTime createdAt,
    List<CommentResponse> replies
) {}
```

## 서비스 계층

### PostService 주요 메서드

| Method | Description |
|--------|-------------|
| `createPost(PostRequest)` | 게시글 생성 |
| `getPosts(Pageable)` | 게시글 목록 조회 |
| `getPost(Long)` | 게시글 상세 조회 |
| `updatePost(Long, PostRequest)` | 게시글 수정 |
| `deletePost(Long)` | 게시글 삭제 |

### CommentService 주요 메서드

| Method | Description |
|--------|-------------|
| `createComment(Long postId, CommentRequest)` | 댓글 생성 |
| `createReply(Long commentId, CommentRequest)` | 대댓글 생성 |
| `getComments(Long postId)` | 게시글 댓글 조회 |
| `updateComment(Long, CommentRequest)` | 댓글 수정 |
| `deleteComment(Long)` | 댓글 삭제 |

## 설계 원칙

1. **계층 분리**: domain, api, common 패키지로 명확히 분리
2. **DTO 분리**: 요청/응답 DTO를 엔티티와 분리하여 API 계약 관리
3. **지연 로딩**: 연관관계는 기본적으로 LAZY 로딩
4. **Cascade**: 부모 엔티티 삭제 시 자식도 함께 삭제
