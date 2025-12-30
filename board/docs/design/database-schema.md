# 데이터베이스 스키마 설계

## 개요
게시판 시스템을 위한 데이터베이스 스키마 설계

## ERD

```
┌─────────────────────┐
│       posts         │
├─────────────────────┤
│ id (PK)             │
│ title               │
│ content             │
│ author              │
│ created_at          │
│ updated_at          │
└─────────┬───────────┘
          │ 1:N
          │
┌─────────▼───────────┐
│      comments       │
├─────────────────────┤
│ id (PK)             │
│ post_id (FK)        │
│ parent_id (FK, self)│──┐ self-reference
│ content             │  │ (대댓글)
│ author              │◄─┘
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

## 테이블 정의

### 1. posts (게시글)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 게시글 ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| author | VARCHAR(50) | NOT NULL | 작성자 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_posts_created_at (created_at DESC) - 최신글 조회용

### 2. comments (댓글/대댓글)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 댓글 ID |
| post_id | BIGINT | FK, NOT NULL | 게시글 ID |
| parent_id | BIGINT | FK, NULLABLE | 부모 댓글 ID (대댓글인 경우) |
| content | TEXT | NOT NULL | 내용 |
| author | VARCHAR(50) | NOT NULL | 작성자 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_comments_post_id (post_id) - 게시글별 댓글 조회
- INDEX idx_comments_parent_id (parent_id) - 대댓글 조회

**외래키:**
- FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
- FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE

## DDL

```sql
-- 게시글 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_posts_created_at (created_at DESC)
);

-- 댓글 테이블 (대댓글 포함)
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    author VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comments_post_id (post_id),
    INDEX idx_comments_parent_id (parent_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);
```

## 설계 결정사항

### 댓글/대댓글 구조
- **Self-referencing 방식** 채택
- `parent_id`가 NULL이면 댓글, 값이 있으면 대댓글
- 장점: 단순한 구조, 확장 용이
- 제한: 대댓글은 1단계만 허용 (depth 제한)

### 삭제 정책
- 게시글 삭제 시 관련 댓글 전체 CASCADE 삭제
- 부모 댓글 삭제 시 대댓글도 CASCADE 삭제
