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
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration  
- `GET /api/auth/me` - Get current user info
- `POST /api/posts` - Create new post (requires authentication)

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