# 게시판 API 설계

## 개요
게시글 등록, 조회, 댓글, 대댓글 기능을 제공하는 REST API 설계

## API 엔드포인트

### 게시글 (Posts)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts` | 게시글 등록 |
| GET | `/api/posts` | 게시글 목록 조회 (페이징) |
| GET | `/api/posts/{id}` | 게시글 상세 조회 |
| PUT | `/api/posts/{id}` | 게시글 수정 |
| DELETE | `/api/posts/{id}` | 게시글 삭제 |

### 댓글 (Comments)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{postId}/comments` | 댓글 등록 |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 조회 |
| PUT | `/api/comments/{id}` | 댓글 수정 |
| DELETE | `/api/comments/{id}` | 댓글 삭제 |

### 대댓글 (Replies)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/comments/{commentId}/replies` | 대댓글 등록 |

---

## 상세 API 명세

### 1. 게시글 등록
```
POST /api/posts
```

**Request Body:**
```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자",
  "createdAt": "2025-12-30T12:00:00",
  "updatedAt": "2025-12-30T12:00:00"
}
```

### 2. 게시글 목록 조회
```
GET /api/posts?page=0&size=10&sort=createdAt,desc
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "게시글 제목",
      "author": "작성자",
      "createdAt": "2025-12-30T12:00:00",
      "commentCount": 5
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

### 3. 게시글 상세 조회
```
GET /api/posts/{id}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "게시글 제목",
  "content": "게시글 내용",
  "author": "작성자",
  "createdAt": "2025-12-30T12:00:00",
  "updatedAt": "2025-12-30T12:00:00",
  "comments": [
    {
      "id": 1,
      "content": "댓글 내용",
      "author": "댓글 작성자",
      "createdAt": "2025-12-30T13:00:00",
      "replies": [
        {
          "id": 2,
          "content": "대댓글 내용",
          "author": "대댓글 작성자",
          "createdAt": "2025-12-30T14:00:00"
        }
      ]
    }
  ]
}
```

### 4. 댓글 등록
```
POST /api/posts/{postId}/comments
```

**Request Body:**
```json
{
  "content": "댓글 내용",
  "author": "작성자"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "postId": 1,
  "content": "댓글 내용",
  "author": "작성자",
  "createdAt": "2025-12-30T13:00:00"
}
```

### 5. 대댓글 등록
```
POST /api/comments/{commentId}/replies
```

**Request Body:**
```json
{
  "content": "대댓글 내용",
  "author": "작성자"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "parentId": 1,
  "content": "대댓글 내용",
  "author": "작성자",
  "createdAt": "2025-12-30T14:00:00"
}
```

---

## 에러 응답

| HTTP Status | Code | Description |
|-------------|------|-------------|
| 400 | BAD_REQUEST | 잘못된 요청 파라미터 |
| 404 | NOT_FOUND | 리소스를 찾을 수 없음 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |

**에러 응답 형식:**
```json
{
  "status": 404,
  "code": "NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다.",
  "timestamp": "2025-12-30T12:00:00"
}
```
