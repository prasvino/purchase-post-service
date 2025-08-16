# 📦 Purchase Post Backend

A Spring Boot backend for a social media app where users post about their purchases (online/offline), similar to Twitter threads.  
Each post can contain:
- Text description
- Media (image/video)
- Product link
- Metadata (price, platform, visibility, etc.)

## 🚀 Features

- **User authentication** with JWT (login & registration)
- **Posts API** (CRUD operations, like/comment/repost counters)
- **Media upload flow**:
    - Generate presigned S3 URLs for upload
    - Background worker listens for S3 `ObjectCreated` events via SQS
    - Updates media status to `READY` and performs thumbnail/transcoding jobs
- **Platform integration** (link posts to platforms like Amazon, Flipkart, etc.)
- **Database**: PostgreSQL with JPA
- **Infrastructure ready** for AWS S3 + SQS

---

## 🏗 Project Structure

```
src/main/java/com/yourapp/
├── PurchasePostBackendApplication.java   # Main entrypoint
├── config/                               # Security, AWS (S3, SQS) configs
├── controller/                           # REST controllers (Auth, Post, Media)
├── dto/                                  # DTOs (UserResponse, PostResponse, etc.)
├── entity/                               # Entities (User, Post, Media, Platform)
├── mapper/                               # MapStruct mappers
├── repository/                           # Spring Data JPA repositories
├── security/                             # JWT utils, filters
├── service/                              # Services (UserService, PostService, MediaService)
└── worker/                               # S3 event listener, media processor
```

---

## ⚙️ Tech Stack

- **Java 17+**
- **Spring Boot 3**
- **Spring Security** (JWT)
- **Spring Data JPA** (PostgreSQL)
- **MapStruct** (DTO mapping)
- **AWS SDK v2** (S3, SQS)
- **Spring Cloud AWS SQS** (for `@SqsListener`)
- **Maven**

---

## 🔑 Setup & Run

### 1. Clone repo
```bash
git clone https://github.com/your-org/purchase-post-backend.git
cd purchase-post-backend
```

### 2. Configure environment
Update `src/main/resources/application.yml` with your AWS + DB config:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/purchasepost
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update

cloud:
  aws:
    region:
      static: us-east-1

app:
  s3:
    bucket: your-media-bucket
    sqsQueue: your-s3-event-queue
jwt:
  secret: change-me
```

### 3. Run
```bash
mvn spring-boot:run
```

### 4. Test APIs
- `POST /auth/register` → Register new user
- `POST /auth/login` → Get JWT token
- `POST /posts` → Create post (requires JWT)
- `GET /posts/{id}` → Get post details

---

## 🧪 Testing

Run all tests:
```bash
mvn test
```

For local dev without AWS:
- Use **profiles** (`dev`, `test`) to disable SQS listeners
- Or run AWS mocks like [LocalStack](https://github.com/localstack/localstack)

---

## 📌 Next Steps

- Add search & feed APIs
- Implement likes/comments/reposts APIs
- Add CI/CD pipeline with GitHub Actions
- Integrate caching (Redis) for hot feeds

---

## 👤 Author

Built by **Prasanna Muthukumaran** ✨  
