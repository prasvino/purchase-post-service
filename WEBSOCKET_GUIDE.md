## 🔌 WebSocket Connection Guide

### 🚨 **Issue Resolved**: WebSocket Connection Failed (400 Error)

The WebSocket connection error was caused by:
1. **Port mismatch**: Backend was misconfigured
2. **CORS restrictions**: WebSocket endpoints needed proper CORS setup
3. **Connection type**: SockJS vs Raw WebSocket confusion

---

### 📋 **Available WebSocket Endpoints:**

#### 1. **SockJS Endpoint (Recommended)**
- **URL**: `http://localhost:8081/ws`
- **Type**: SockJS with fallback support
- **Use when**: You want automatic fallback and better compatibility

#### 2. **Raw WebSocket Endpoint**
- **URL**: `ws://localhost:8081/ws-raw`
- **Type**: Pure WebSocket connection
- **Use when**: You need direct WebSocket connection

---

### 🔧 **Frontend Connection Examples:**

#### Option 1: Using SockJS (Recommended)
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8081/ws');
const stompClient = new Client({
  webSocketFactory: () => socket,
  debug: (str) => console.log(str),
  onConnect: (frame) => {
    console.log('Connected: ' + frame);
    
    // Subscribe to topics
    stompClient.subscribe('/topic/posts', (message) => {
      const data = JSON.parse(message.body);
      console.log('Received:', data);
    });
  },
  onStompError: (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
  }
});

stompClient.activate();
```

#### Option 2: Raw WebSocket
```javascript
const websocket = new WebSocket('ws://localhost:8081/ws-raw');

websocket.onopen = () => {
  console.log('WebSocket Connected');
};

websocket.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Received:', data);
};

websocket.onerror = (error) => {
  console.error('WebSocket Error:', error);
};

websocket.onclose = () => {
  console.log('WebSocket Disconnected');
};
```

#### Option 3: Using Socket.IO Client (if you prefer Socket.IO style)
```javascript
import { io } from 'socket.io-client';

// Note: This connects to the SockJS endpoint with Socket.IO compatibility
const socket = io('http://localhost:8081', {
  path: '/ws',
  transports: ['websocket', 'polling']
});

socket.on('connect', () => {
  console.log('Connected via Socket.IO');
});

socket.on('message', (data) => {
  console.log('Received:', data);
});
```

---

### 📨 **Subscribing to Topics:**

The backend sends messages on these topics:

```javascript
// Subscribe to post updates
stompClient.subscribe('/topic/posts', (message) => {
  const update = JSON.parse(message.body);
  // Handle: NEW_POST, POST_LIKED, POST_REPOSTED, etc.
});

// Subscribe to user updates  
stompClient.subscribe('/topic/users', (message) => {
  const update = JSON.parse(message.body);
  // Handle: USER_FOLLOWED, USER_UPDATED, etc.
});

// Subscribe to trending updates
stompClient.subscribe('/topic/trending', (message) => {
  const update = JSON.parse(message.body);
  // Handle: TRENDING_UPDATED, STATS_UPDATED
});
```

---

### 🎯 **Message Format:**

Backend sends messages in this format:
```json
{
  "type": "POST_LIKED",
  "payload": {
    "postId": "uuid-string",
    "userId": "uuid-string", 
    "likesCount": 42
  },
  "timestamp": "2023-08-23T17:30:00Z"
}
```

---

### ✅ **Fixed Configuration:**

1. **✅ Backend Port**: Now consistently on `8081`
2. **✅ CORS Origins**: Allows `localhost:8082` (your frontend)
3. **✅ WebSocket Security**: Endpoint `/ws/**` is permitted in security config
4. **✅ Dual Endpoints**: Both SockJS and raw WebSocket available

---

### 🧪 **Quick Test:**

After connecting, you should see these in browser console:
```
Connected: CONNECTED
WebSocket connection established
```

If you still see connection errors, check:
1. Backend is running on port **8081**
2. Frontend is trying to connect to correct URL
3. No firewall blocking the connection