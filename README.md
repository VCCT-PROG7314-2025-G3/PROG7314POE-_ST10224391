# SwopTrader - Sustainable Trading Platform

## Overview

SwopTrader is a comprehensive mobile trading platform built with Android Jetpack Compose that enables users to trade items sustainably while reducing carbon footprint. The app connects to a custom REST API with MongoDB Atlas database, providing real-time synchronization and offline capabilities.

## 🚀 Key Features

### Core Trading Features
- **Item Listing & Management**: Create, edit, and manage item listings with photos, descriptions, and categories
- **Smart Matching**: AI-powered item matching based on user preferences and trade history
- **Offer System**: Send, receive, accept, and reject trade offers with cash differences
- **Meetup Scheduling**: Integrated location-based meetup scheduling with Google Maps
- **Trade History**: Complete transaction tracking with carbon savings and trade scores
- **User Profiles**: Comprehensive user profiles with trade scores, verification, and statistics

### Advanced Features
- **Real-time Chat**: In-app messaging system with offer integration
- **Push Notifications**: Real-time notifications for offers, messages, and trade updates
- **Offline Mode**: Full offline functionality with automatic synchronization
- **Multi-language Support**: Support for English, Afrikaans, and Zulu
- **Biometric Authentication**: Fingerprint and facial recognition security
- **Location Services**: GPS-based location tracking and distance calculations
- **Image Management**: Multiple photo uploads with cloud storage
- **Search & Filtering**: Advanced search with category and location filters

## 🏗️ Architecture

### Frontend (Android)
- **Framework**: Android Jetpack Compose with MVVM architecture
- **State Management**: StateFlow and MutableStateFlow for reactive UI
- **Dependency Injection**: Hilt for clean architecture
- **Local Database**: Room database for offline storage
- **Real-time Sync**: Firebase Firestore for real-time updates
- **Maps Integration**: Google Maps API for location services
- **Image Loading**: Coil for efficient image loading and caching

### Backend (REST API)
- **Framework**: Node.js with Express.js
- **Database**: MongoDB Atlas (cloud-hosted)
- **Authentication**: JWT-based authentication
- **Security**: CORS, Helmet, Rate limiting
- **Deployment**: Railway cloud platform
- **API Documentation**: RESTful endpoints with comprehensive error handling

## 📱 Requirements Implementation

### ✅ Single Sign-On (SSO)
- **Email Authentication**: Secure email/password registration and login
- **Firebase Integration**: Firebase Authentication for secure user management
- **Session Management**: Persistent login sessions with automatic token refresh

### ✅ Biometric Authentication
- **Fingerprint Recognition**: Android BiometricPrompt API integration
- **Facial Recognition**: Face unlock support for compatible devices
- **Secure Storage**: Biometric-protected secure storage for sensitive data
- **Fallback Options**: PIN/password fallback when biometrics unavailable

### ✅ Settings Management
- **User Preferences**: Comprehensive settings for notifications, privacy, and app behavior
- **Profile Management**: Edit profile information, photos, and preferences
- **Language Selection**: Dynamic language switching with real-time UI updates
- **Notification Settings**: Granular control over push notification types

### ✅ REST API with Database
- **Custom API**: Node.js/Express REST API with MongoDB Atlas
- **Database Schema**: Comprehensive data models for users, items, offers, chats, and trades
- **API Endpoints**: Full CRUD operations for all entities
- **Error Handling**: Robust error handling with proper HTTP status codes
- **Data Validation**: Input validation and sanitization

### ✅ Offline Mode with Sync
- **Room Database**: Local SQLite database for offline storage
- **Sync Manager**: Automatic synchronization when connection restored
- **Conflict Resolution**: Smart conflict resolution for concurrent edits
- **Offline Indicators**: Clear UI indicators showing offline/online status
- **Queue System**: Offline action queue with automatic retry

### ✅ Real-time Notifications
- **Firebase Cloud Messaging**: Push notification system
- **Notification Types**: Messages, offers, trade updates, and system alerts
- **Custom Channels**: Categorized notification channels for better user control
- **Deep Linking**: Notifications link directly to relevant app sections

### ✅ Multi-language Support
- **South African Languages**: English, Afrikaans, and Zulu support
- **Dynamic Translation**: Real-time language switching without app restart
- **Cultural Adaptation**: Region-specific formatting and cultural considerations
- **Accessibility**: Full accessibility support for all languages

## 🛠️ Technical Implementation

### Data Flow Architecture
```
User Interface (Compose) 
    ↓
ViewModel (MVVM)
    ↓
Repository Pattern
    ↓
┌─────────────────┬─────────────────┐
│   REST API      │   Firebase      │
│   (Primary)     │   (Fallback)    │
└─────────────────┴─────────────────┘
    ↓
MongoDB Atlas Database
```

### API Integration Strategy
The app implements a **gradual migration approach**:
1. **Primary**: REST API calls for all data operations
2. **Fallback**: Firebase Firestore if API fails
3. **Local**: Room database for offline support
4. **Caching**: Automatic local caching of API responses

### Security Features
- **Data Encryption**: All sensitive data encrypted at rest and in transit
- **API Security**: Rate limiting, CORS protection, and input validation
- **Authentication**: JWT tokens with automatic refresh
- **Privacy**: GDPR-compliant data handling and user consent

## 📊 Database Schema

### Core Entities
- **Users**: Profile information, preferences, and statistics
- **Items**: Product listings with images, descriptions, and metadata
- **Offers**: Trade proposals with cash differences and status tracking
- **Chats**: Real-time messaging with offer integration
- **Meetups**: Location-based meeting scheduling
- **Trade History**: Complete transaction records with analytics

## 🚀 Deployment

### Android App
- **Build**: Gradle-based build system with automated testing
- **Distribution**: APK generation for testing and production
- **Performance**: Optimized for various Android versions and screen sizes

### REST API
- **Platform**: Railway cloud deployment
- **URL**: `https://swoptrader-api-production.up.railway.app`
- **Monitoring**: Health checks and performance monitoring
- **Scaling**: Auto-scaling based on demand

## 📱 App Features List

### User Management
- User registration and authentication
- Profile creation and management
- Biometric security setup
- Settings and preferences
- Multi-language support

### Item Management
- Create and edit item listings
- Photo upload and management
- Category and condition selection
- Location-based listing
- Search and filtering

### Trading System
- Send and receive trade offers
- Cash difference negotiation
- Offer acceptance/rejection
- Counter-offer functionality
- Trade history tracking

### Communication
- Real-time chat messaging
- In-chat offer management
- Push notifications
- Message history
- User verification

### Meetup System
- Location-based meetup scheduling
- Google Maps integration
- Time and date selection
- Meetup status tracking
- Completion verification

### Analytics & Scoring
- Trade score calculation
- Carbon footprint tracking
- User level progression
- Statistics dashboard
- Achievement system

### Offline Capabilities
- Offline item browsing
- Offline chat (queued)
- Offline offer creation
- Automatic sync on reconnect
- Conflict resolution

## 🔧 Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Node.js 16+ for API development
- MongoDB Atlas account
- Google Maps API key
- Firebase project setup

### Installation
1. Clone the repository
2. Set up Firebase project and add `google-services.json`
3. Configure Google Maps API key in `strings.xml`
4. Set up MongoDB Atlas connection
5. Deploy REST API to Railway
6. Build and run the Android app

## 📈 Performance & Scalability

- **Optimized UI**: Jetpack Compose for smooth 60fps performance
- **Efficient Data Loading**: Pagination and lazy loading
- **Image Optimization**: Compressed images with caching
- **Network Optimization**: Request batching and caching
- **Memory Management**: Proper lifecycle management and memory leaks prevention

## 🔒 Security & Privacy

- **Data Protection**: End-to-end encryption for sensitive data
- **User Privacy**: GDPR-compliant data handling
- **Secure Authentication**: Multi-factor authentication support
- **API Security**: Rate limiting and input validation
- **Biometric Security**: Hardware-backed biometric authentication

## 🌱 Sustainability Features

- **Carbon Tracking**: Calculate and display carbon savings from trades
- **Environmental Impact**: Promote reuse and reduce waste
- **Local Trading**: Encourage local meetups to reduce transportation
- **Sustainability Scoring**: Gamify environmental consciousness

## 📞 Support & Contact

For technical support or feature requests, please contact the development team.

---

**SwopTrader** - Making sustainable trading accessible, secure, and user-friendly.