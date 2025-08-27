## 🔐 Login/Register API Testing Guide

### Available Test Users:
1. **John Doe**
   - Email: `john@example.com`
   - Password: `password123`
   - Username: `johndoe`

2. **Jane Smith**
   - Email: `jane@example.com`
   - Password: `password123`
   - Username: `janesmith`

3. **Mike Johnson**
   - Email: `mike@example.com`
   - Password: `password123`
   - Username: `mikejohnson`

### 🧪 Test the APIs:

#### 1. Login Request:
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### 2. Register Request:
```bash
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "name": "Test User",
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

#### 3. Get Current User (requires token):
```bash
GET http://localhost:8081/api/auth/me
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 4. Create Post (requires token):
```bash
POST http://localhost:8081/api/posts
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>

{
  "text": "Just bought this amazing product! Loving it so far 😍",
  "purchaseDate": "2024-08-23",
  "price": 29.99,
  "currency": "USD",
  "platformId": null,
  "productUrl": "https://example.com/product",
  "mediaIds": [],
  "visibility": "PUBLIC"
}
```

**Required Fields for Post Creation:**
- `text` (String): Must not be blank - the post content
- `purchaseDate` (String): Must not be null - date in YYYY-MM-DD format
- `price` (Number): Must not be null - the purchase price
- `currency` (String): Must not be blank - currency code (e.g., "USD", "EUR")

**Optional Fields:**
- `platformId` (UUID): The platform where the purchase was made
- `productUrl` (String): URL to the product
- `mediaIds` (Array): List of media attachment UUIDs
- `visibility` (String): Post visibility ("PUBLIC", "PRIVATE", "FOLLOWERS_ONLY")

#### 5. Like Post (requires token):
```bash
POST http://localhost:8081/api/posts/{postId}/like
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 6. Repost Post (requires token):
```bash
POST http://localhost:8081/api/posts/{postId}/repost
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 7. Share Post (requires token):
```bash
POST http://localhost:8081/api/posts/{postId}/share
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

## 📝 Comments API

#### 8. Create Comment (requires token):
```bash
POST http://localhost:8081/api/posts/{postId}/comments
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>

{
  "text": "Great choice! I bought the same product last month and love it!",
  "parentCommentId": null
}
```

#### 9. Get Comments for Post:
```bash
GET http://localhost:8081/api/posts/{postId}/comments?page=0&size=10
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 10. Get Top-Level Comments (no replies):
```bash
GET http://localhost:8081/api/posts/{postId}/comments/top-level
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 11. Get Comment Replies:
```bash
GET http://localhost:8081/api/comments/{commentId}/replies
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 12. Update Comment (requires token, author only):
```bash
PUT http://localhost:8081/api/comments/{commentId}
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>

{
  "text": "Updated comment text here"
}
```

#### 13. Delete Comment (requires token, author only):
```bash
DELETE http://localhost:8081/api/comments/{commentId}
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 14. Like Comment (requires token):
```bash
POST http://localhost:8081/api/comments/{commentId}/like
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

#### 15. Get User Comments:
```bash
GET http://localhost:8081/api/users/{userId}/comments?page=0&size=10
Authorization: Bearer <JWT_TOKEN_FROM_LOGIN>
```

### Expected Response Format:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "uuid-string",
    "name": "User Name",
    "username": "username",
    "avatar": "avatar-url",
    "bio": "User bio",
    "location": "User location",
    "website": "User website",
    "joinedAt": "2023-08-23T17:30:00Z",
    "isVerified": true,
    "followersCount": 0,
    "followingCount": 0,
    "postsCount": 0,
    "totalSpent": 0.0,
    "avgRating": 0.0,
    "isOnline": true
  }
}
```

### ✅ API Endpoints Summary:
**Authentication:**
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration  
- `GET /api/auth/me` - Get current user info

**Posts:**
- `POST /api/posts` - Create new post (requires authentication)
- `GET /api/posts` - Get all posts with pagination
- `GET /api/posts/{id}` - Get specific post
- `GET /api/posts/user/{userId}` - Get user's posts
- `POST /api/posts/{postId}/like` - Like a post
- `POST /api/posts/{postId}/repost` - Repost a post
- `POST /api/posts/{postId}/share` - Share a post

**Comments:**
- `POST /api/posts/{postId}/comments` - Create comment on post
- `GET /api/posts/{postId}/comments` - Get comments for post
- `GET /api/posts/{postId}/comments/top-level` - Get top-level comments
- `GET /api/comments/{commentId}/replies` - Get comment replies
- `GET /api/comments/{commentId}` - Get specific comment
- `PUT /api/comments/{commentId}` - Update comment (author only)
- `DELETE /api/comments/{commentId}` - Delete comment (author only)
- `POST /api/comments/{commentId}/like` - Like a comment
- `GET /api/users/{userId}/comments` - Get user's comments

### CORS Configuration:
✅ Supports requests from:
- http://localhost:8080 (Your frontend - NEW!)
- http://localhost:8082 (Alternative frontend port)
- http://localhost:3000 (React default)
- http://localhost:5173 (Vite default)

### WebSocket Configuration:
✅ **Fixed WebSocket Connection Issues!**

**SockJS Endpoint**: `http://localhost:8081/ws`
**Raw WebSocket**: `ws://localhost:8081/ws-raw`

**Topics Available**:
- `/topic/posts` - Post updates (likes, reposts, new posts)
- `/topic/users` - User updates (follows, profile changes)
- `/topic/trending` - Trending and stats updates

**See WEBSOCKET_GUIDE.md for detailed connection examples!**