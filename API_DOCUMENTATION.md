# SwopTrader REST API Documentation

## Overview

The SwopTrader REST API is a comprehensive backend service built with Node.js, Express.js, and MongoDB Atlas. It provides secure, scalable endpoints for the SwopTrader mobile application, supporting all core trading functionality with real-time capabilities.

## 🚀 API Information

- **Base URL**: `https://swoptrader-api-production.up.railway.app/api/v1`
- **Protocol**: HTTPS
- **Authentication**: JWT-based authentication
- **Database**: MongoDB Atlas (Cloud-hosted)
- **Deployment**: Railway Cloud Platform

## 🏗️ Architecture

### Technology Stack
- **Runtime**: Node.js 16+
- **Framework**: Express.js
- **Database**: MongoDB Atlas with Mongoose ODM
- **Security**: Helmet, CORS, Rate Limiting
- **Deployment**: Railway with auto-scaling

### Database Schema
The API uses MongoDB with the following collections:
- `users` - User profiles and authentication data
- `items` - Product listings and metadata
- `offers` - Trade proposals and negotiations
- `chats` - Real-time messaging data
- `messages` - Individual chat messages
- `trades` - Completed trade history

## 📡 API Endpoints

### Health Check
```
GET /health
```
Returns API status and basic information.

### User Management

#### Get User by ID
```
GET /users/{userId}
```
Retrieves user profile information.

#### Create User
```
POST /users
Content-Type: application/json

{
  "id": "user_123",
  "name": "John Doe",
  "email": "john@example.com",
  "profileImageUrl": "https://example.com/avatar.jpg",
  "location": {
    "latitude": -26.2041,
    "longitude": 28.0473,
    "address": "Johannesburg, South Africa"
  },
  "tradeScore": 150,
  "level": 3,
  "carbonSaved": 25.5,
  "isVerified": true
}
```

#### Update User
```
PUT /users/{userId}
Content-Type: application/json

{
  "name": "John Smith",
  "tradeScore": 175
}
```

### Item Management

#### Get Items (Paginated)
```
GET /items?page=1&limit=20&category=electronics&search=camera&ownerId=user_123
```
Retrieves paginated list of items with optional filtering.

#### Get Item by ID
```
GET /items/{itemId}
```
Retrieves specific item details.

#### Create Item
```
POST /items
Content-Type: application/json

{
  "id": "item_123",
  "name": "Vintage Camera",
  "description": "Canon EOS 5D Mark III in excellent condition",
  "category": "electronics",
  "condition": "good",
  "images": ["https://example.com/image1.jpg"],
  "ownerId": "user_123",
  "location": {
    "latitude": -26.2041,
    "longitude": 28.0473,
    "address": "Johannesburg, South Africa"
  },
  "desiredTrades": ["laptop", "guitar"],
  "isAvailable": true
}
```

#### Update Item
```
PUT /items/{itemId}
Content-Type: application/json

{
  "name": "Updated Camera Name",
  "isAvailable": false
}
```

#### Delete Item
```
DELETE /items/{itemId}
```
Removes item from the system.

### Offer Management

#### Get Offers (Paginated)
```
GET /offers?page=1&limit=20&status=pending&userId=user_123
```
Retrieves paginated list of offers with filtering.

#### Get Offer by ID
```
GET /offers/{offerId}
```
Retrieves specific offer details.

#### Create Offer
```
POST /offers
Content-Type: application/json

{
  "id": "offer_123",
  "fromUserId": "user_123",
  "toUserId": "user_456",
  "requestedItemId": "item_789",
  "offeredItemIds": ["item_101", "item_102"],
  "status": "pending",
  "message": "I'm interested in trading for your camera",
  "cashAmount": 50.00,
  "meetup": {
    "id": "meetup_123",
    "offerId": "offer_123",
    "location": {
      "latitude": -26.2041,
      "longitude": 28.0473,
      "address": "Johannesburg, South Africa",
      "type": "specific_location"
    },
    "scheduledTime": 1640995200000,
    "status": "scheduled",
    "createdAt": 1640908800000,
    "updatedAt": 1640908800000
  }
}
```

#### Update Offer
```
PUT /offers/{offerId}
Content-Type: application/json

{
  "status": "accepted",
  "updatedAt": 1640995200000
}
```

### Chat Management

#### Get User Chats
```
GET /chats?userId=user_123
```
Retrieves all chats for a specific user.

#### Get Chat Messages
```
GET /chats/{chatId}/messages?page=1&limit=50
```
Retrieves paginated chat messages.

#### Send Message
```
POST /chats/{chatId}/messages
Content-Type: application/json

{
  "id": "msg_123",
  "chatId": "chat_456",
  "tradeId": "offer_789",
  "senderId": "user_123",
  "receiverId": "user_456",
  "message": "Hello, I'm interested in your item",
  "type": "text",
  "timestamp": 1640995200000
}
```

### Trade History

#### Get Trade History
```
GET /trades/history?userId=user_123
```
Retrieves user's trade history.

#### Create Trade History Entry
```
POST /trades/history
Content-Type: application/json

{
  "id": "trade_123",
  "offerId": "offer_456",
  "participantIds": ["user_123", "user_456"],
  "itemsTraded": [
    {
      "itemId": "item_789",
      "userId": "user_123",
      "itemName": "Vintage Camera",
      "itemImage": "https://example.com/camera.jpg"
    }
  ],
  "completedAt": 1640995200000,
  "meetupId": "meetup_789",
  "carbonSaved": 5.0,
  "tradeScoreEarned": 25
}
```

## 🔒 Security Features

### Authentication
- JWT-based authentication for secure API access
- Token expiration and refresh mechanisms
- Secure password hashing with bcrypt

### Rate Limiting
- 100 requests per 15-minute window per IP
- Prevents API abuse and ensures fair usage
- Configurable limits for different endpoint types

### CORS Protection
- Configurable CORS policies
- Support for multiple origins
- Credential handling for authenticated requests

### Input Validation
- Comprehensive input validation and sanitization
- SQL injection prevention
- XSS protection
- Data type validation

## 📊 Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data
  },
  "message": "Operation completed successfully"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message",
  "code": "ERROR_CODE",
  "details": {
    // Additional error details
  }
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "data": [
      // Array of items
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "pages": 5
    }
  }
}
```

## 🚀 Performance & Scalability

### Database Optimization
- Indexed queries for fast data retrieval
- Connection pooling for efficient database usage
- Query optimization for large datasets

### Caching Strategy
- Response caching for frequently accessed data
- Redis integration for session management
- CDN support for static assets

### Auto-scaling
- Railway platform auto-scaling based on demand
- Load balancing for high availability
- Health checks and monitoring

## 🔧 Development & Deployment

### Environment Variables
```bash
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/swoptrader
PORT=3000
NODE_ENV=production
ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

### Local Development
```bash
npm install
npm run dev
```

### Production Deployment
- Automated deployment via Railway
- Environment variable management
- SSL certificate handling
- Domain configuration

## 📈 Monitoring & Analytics

### Health Monitoring
- Real-time health checks
- Performance metrics tracking
- Error rate monitoring
- Uptime monitoring

### Logging
- Structured logging with timestamps
- Error tracking and reporting
- Request/response logging
- Performance profiling

## 🔄 Integration with Mobile App

### API-First Strategy
The mobile app implements a gradual migration approach:
1. **Primary**: REST API calls for all data operations
2. **Fallback**: Firebase Firestore if API fails
3. **Local**: Room database for offline support
4. **Caching**: Automatic local caching of API responses

### Offline Support
- Queue system for offline actions
- Automatic synchronization when online
- Conflict resolution for concurrent edits
- Data consistency across devices

## 🌐 Future Enhancements

### Planned Features
- WebSocket support for real-time updates
- GraphQL API for flexible data querying
- Microservices architecture
- Advanced analytics and reporting
- Machine learning integration

### Scalability Roadmap
- Horizontal scaling with load balancers
- Database sharding for large datasets
- CDN integration for global performance
- Multi-region deployment

---

This API documentation provides comprehensive information about the SwopTrader REST API, its capabilities, and integration with the mobile application. The API is designed for scalability, security, and performance to support the growing needs of the SwopTrader platform.
