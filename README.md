# Board API

Spring Boot 기반 게시판 REST API 백엔드 서비스입니다.

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **H2 Database** (개발/테스트용)
- **Lombok**

## 프로젝트 구조

```
board/
├── src/main/java/io/github/tato126/board/
│   ├── api/                    # REST Controller & DTO
│   │   ├── post/               # 게시글 API
│   │   └── comment/            # 댓글 API
│   ├── domain/                 # 도메인 엔티티 & 서비스
│   │   ├── post/               # 게시글 도메인
│   │   ├── comment/            # 댓글 도메인
│   │   └── common/             # 공통 엔티티 (BaseEntity)
│   ├── common/                 # 공통 유틸리티
│   │   ├── exception/          # 예외 처리
│   │   └── response/           # 응답 형식
│   └── config/                 # 설정 클래스
└── src/test/java/              # 테스트 코드
```

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
| POST | `/api/comments/{commentId}/replies` | 대댓글 등록 |

## 실행 방법

```bash
cd board
./gradlew bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

## 테스트 실행

```bash
cd board
./gradlew test
```

## API 사용 예시

### 게시글 등록

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "게시글 제목",
    "content": "게시글 내용",
    "author": "작성자"
  }'
```

### 게시글 목록 조회

```bash
curl http://localhost:8080/api/posts?page=0&size=10&sort=createdAt,desc
```

### 댓글 등록

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "댓글 내용",
    "author": "댓글 작성자"
  }'
```

## 문서

- [API 설계](board/docs/design/api-design.md)
- [데이터베이스 스키마](board/docs/design/database-schema.md)
- [도메인 모델](board/docs/design/domain-model.md)
