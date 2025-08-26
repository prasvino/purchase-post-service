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
- **Real-time updates** with WebSocket support
- **Trending items** and statistics endpoints
- **User profiles** with follow functionality
- **Platform integration** (link posts to platforms like Amazon, Flipkart, etc.)
- **Media upload flow**:
    - Generate presigned S3 URLs for upload
    - Background worker listens for S3 `ObjectCreated` events via SQS
    - Updates media status to `READY` and performs thumbnail/transcoding jobs
- **Database**: H2 (for development) with JPA
- **Infrastructure ready** for AWS S3 + SQS

---

## 🏗 Project Structure

```
src/main/java/com/app/
├── Application.java                       # Main entrypoint
├── auth/                                 # Authentication (DTOs, Service, Controller, Security)
├── config/                               # Security, CORS, WebSocket configs
├── controller/                           # REST controllers (Auth, Post, Media, User, Trending, Platform)
├── dto/                                  # DTOs (UserResponse, PostResponse, etc.)
├── entity/                               # Entities (User, Post, Media, Platform)
├── init/                                 # Data initialization
├── mapper/                               # MapStruct mappers
├── repository/                           # Spring Data JPA repositories
├── service/                              # Services (UserService, PostService, MediaService, TrendingService)
├── trending/                             # Trending items and stats
└── worker/                               # S3 event listener, media processor
```

---

## ⚙️ Tech Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT)
- **Spring Data JPA** (H2 for development)
- **Spring WebSocket** (real-time updates)
- **MapStruct** (DTO mapping)
- **AWS SDK v2** (S3, SQS)
- **Spring Cloud AWS SQS** (for `@SqsListener`)
- **Maven**

---

## 🔑 Setup & Run

### 1. Clone repo
```bash
git clone https://github.com/prasvino/purchase-post-service.git
cd purchase-post-service
```

### 2. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Test APIs

#### Authentication
- `POST /api/auth/register` → Register new user
- `POST /api/auth/login` → Get JWT token
- `GET /api/auth/me` → Get current user profile

#### Posts
- `GET /api/posts?page=1&limit=10` → Get paginated posts
- `POST /api/posts` → Create post (requires JWT)
- `GET /api/posts/{id}` → Get post details
- `POST /api/posts/{id}/like` → Like a post
- `POST /api/posts/{id}/repost` → Repost a post

#### Users
- `GET /api/users/{username}` → Get user profile
- `POST /api/users/{userId}/follow` → Follow a user

#### Trending & Stats
- `GET /api/trending` → Get trending items
- `GET /api/stats` → Get platform statistics
- `GET /api/platforms` → Get available platforms

#### WebSocket
- Connect to `ws://localhost:8080/ws` for real-time updates

### 4. H2 Console
Access the H2 database console at:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:purchase_post_db`
- User: `sa`
- Password: (leave empty)

---

## 🧪 Sample Data

The application automatically initializes with sample data including:
- 3 sample users (johndoe, janesmith, mikejohnson)
- 3 platforms (Amazon, eBay, Best Buy)
- 5 sample posts with interactions

---

## 🔌 Frontend Integration

This backend is designed to work with the Buy It Share It frontend. The frontend expects:

- **Base URL**: `http://localhost:8080/api`
- **WebSocket URL**: `ws://localhost:8080/ws`
- **CORS**: Configured for `http://localhost:3000`

### Authentication Flow
1. Frontend sends login/register request to `/api/auth/*`
2. Backend returns JWT token
3. Frontend includes token in `Authorization: Bearer <token>` header
4. Backend validates token for protected endpoints

### Real-time Updates
- Backend sends WebSocket messages on `/topic/posts`
- Frontend listens for: NEW_POST, POST_LIKED, POST_REPOSTED, USER_FOLLOWED, TRENDING_UPDATED, STATS_UPDATED

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
- Implement comments APIs
- Add CI/CD pipeline with GitHub Actions
- Integrate caching (Redis) for hot feeds
- Add file upload functionality
- Implement proper user authentication context

---

## 👤 Author

Built by **Prasanna Muthukumaran** ✨  
