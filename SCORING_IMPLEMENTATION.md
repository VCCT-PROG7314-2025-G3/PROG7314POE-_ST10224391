# SwopTrader - Scoring System Implementation

### 1. Enhanced Carbon Footprint Calculation

#### Algorithm
The carbon footprint calculation now considers multiple factors:

- **Base Carbon Savings**: 3.0kg CO2 per item
- **Category Multipliers**: Different item categories have different environmental impacts
- **Condition Multipliers**: Better condition items save more carbon
- **Bulk Trade Bonus**: 10% bonus for multiple item trades

#### Category Multipliers
```kotlin
val categoryMultipliers = mapOf(
    "electronics" to 1.5,    // High carbon footprint
    "furniture" to 1.3,      // Carbon intensive manufacturing
    "clothing" to 1.2,       // Fast fashion impact
    "books" to 0.8,          // Lower carbon footprint
    "sports" to 1.1,         // Moderate impact
    "tools" to 1.4,          // Carbon intensive manufacturing
    "art" to 0.9,            // Moderate impact
    "music" to 1.2,          // Moderate impact
    "garden" to 0.7,         // Often sustainable
    "automotive" to 2.0,     // Very high impact
    "home" to 1.1,           // Moderate impact
    "accessories" to 1.0,    // Standard impact
    "other" to 1.0           // Default multiplier
)
```

#### Condition Multipliers
```kotlin
val conditionMultipliers = mapOf(
    "new" to 1.0,
    "like_new" to 0.9,
    "good" to 0.8,
    "fair" to 0.6,
    "poor" to 0.4
)
```

### 2. Enhanced Trade Score Calculation

#### Algorithm
The trade score system rewards various aspects of trading:

- **Base Score**: 15 points for completing a trade
- **Category Bonuses**: Different categories have different values
- **Condition Bonuses**: Better condition items earn more points
- **Multiple Item Bonus**: 2 points per additional item
- **Quality Bonus**: Items with multiple images suggest quality

#### Category Bonuses
```kotlin
val categoryBonuses = mapOf(
    "electronics" to 5,      // Valuable items
    "furniture" to 4,        // Substantial items
    "clothing" to 2,         // Common items
    "books" to 3,            // Educational value
    "sports" to 4,           // Valuable equipment
    "tools" to 5,            // Practical items
    "art" to 6,              // Cultural value
    "music" to 5,            // Valuable instruments
    "garden" to 3,           // Useful items
    "automotive" to 8,       // Very valuable parts
    "home" to 3,             // Useful items
    "accessories" to 2,      // Common items
    "other" to 2             // Default bonus
)
```

#### Condition Bonuses
```kotlin
val conditionBonuses = mapOf(
    "new" to 5,
    "like_new" to 4,
    "good" to 3,
    "fair" to 2,
    "poor" to 1
)
```

### 3. User Level System

#### Level Calculation
Users progress through 10 levels based on their total trade score:

```kotlin
private fun calculateUserLevel(tradeScore: Int): Int {
    return when {
        tradeScore >= 1000 -> 10  // Master Trader
        tradeScore >= 800 -> 9    // Expert Trader
        tradeScore >= 600 -> 8    // Advanced Trader
        tradeScore >= 450 -> 7    // Skilled Trader
        tradeScore >= 350 -> 6    // Experienced Trader
        tradeScore >= 250 -> 5    // Competent Trader
        tradeScore >= 150 -> 4    // Intermediate Trader
        tradeScore >= 100 -> 3    // Novice Trader
        tradeScore >= 50 -> 2     // Beginner Trader
        else -> 1                 // New Trader
    }
}
```

#### Level Benefits
- **Level 1-2**: New/Beginner Trader
- **Level 3-4**: Novice/Intermediate Trader
- **Level 5-6**: Competent/Experienced Trader
- **Level 7-8**: Skilled/Advanced Trader
- **Level 9-10**: Expert/Master Trader

### 4. Automatic User Updates

#### Trade Completion Flow
When a trade is completed:

1. **Calculate Scores**: Carbon saved and trade score earned
2. **Create Trade History**: Record the completed trade
3. **Update Both Users**: Update scores, levels, and carbon saved
4. **Success Message**: Notify users of level progression

#### User Statistics Updates
```kotlin
private fun calculateNewUserStats(user: User, tradeHistory: TradeHistory): User {
    val newCarbonSaved = user.carbonSaved + tradeHistory.carbonSaved
    val newTradeScore = user.tradeScore + tradeHistory.tradeScoreEarned
    val newLevel = calculateUserLevel(newTradeScore)
    
    return user.copy(
        tradeScore = newTradeScore,
        level = newLevel,
        carbonSaved = newCarbonSaved,
        lastActive = System.currentTimeMillis()
    )
}
```

## 🗄️ Database Optimization

### 1. Database Indexes

#### User Indexes
- `id` (unique) - Primary key
- `email` (unique) - Authentication
- `tradeScore` (descending) - Leaderboards
- `level` (descending) - Level-based queries
- `carbonSaved` (descending) - Environmental leaderboards
- `createdAt` (descending) - Recent users

#### Item Indexes
- `id` (unique) - Primary key
- `ownerId` - User's items
- `category` - Category filtering
- `isAvailable` - Available items
- `createdAt` (descending) - Recent items
- `name, description` (text) - Full-text search
- `ownerId, isAvailable` (compound) - User's available items

#### Offer Indexes
- `id` (unique) - Primary key
- `fromUserId` - Sent offers
- `toUserId` - Received offers
- `status` - Status filtering
- `requestedItemId` - Item-based queries
- `createdAt` (descending) - Recent offers
- `fromUserId, status` (compound) - User's sent offers by status
- `toUserId, status` (compound) - User's received offers by status

#### Chat Indexes
- `id` (unique) - Primary key
- `participantIds` - User's chats
- `lastMessageAt` (descending) - Chat ordering
- `createdAt` (descending) - Recent chats

#### ChatMessage Indexes
- `id` (unique) - Primary key
- `chatId` - Chat messages
- `senderId` - User's messages
- `timestamp` (descending) - Message ordering
- `chatId, timestamp` (compound) - Chat messages ordered

#### TradeHistory Indexes
- `id` (unique) - Primary key
- `participantIds` - User's trades
- `completedAt` (descending) - Recent trades
- `offerId` - Offer-based queries
- `carbonSaved` (descending) - Environmental stats

### 2. Database Cleanup

#### Orphaned Items Cleanup
Added admin endpoint to remove items not attributed to valid users:

```javascript
DELETE /api/v1/admin/cleanup-orphaned-items
```

This endpoint:
1. Finds all unique `ownerId` values in items
2. Compares with existing user IDs
3. Deletes items with orphaned owner IDs
4. Returns cleanup statistics

## 🚀 Performance Benefits

### 1. Query Optimization
- **Faster Searches**: Text indexes for item search
- **Efficient Filtering**: Category and status indexes
- **Quick User Lookups**: User ID and email indexes
- **Leaderboard Performance**: Score and level indexes

### 2. Scalability
- **Compound Indexes**: Multi-field queries optimized
- **Descending Indexes**: Recent items/queries optimized
- **Unique Constraints**: Data integrity maintained

### 3. Data Integrity
- **Orphaned Item Cleanup**: Maintains referential integrity
- **Automatic Indexing**: Indexes created on database connection
- **Error Handling**: Comprehensive error handling for all operations

## 📊 Usage Examples

### Carbon Footprint Calculation
```kotlin
// Example: Trading a "good" condition electronics item for a "new" condition book
val carbonSaved = calculateCarbonSaved(requestedItem, offeredItems)
// Result: 3.0 * 1.5 * 0.8 + 3.0 * 0.8 * 1.0 = 3.6 + 2.4 = 6.0kg CO2 saved
```

### Trade Score Calculation
```kotlin
// Example: Same trade as above
val tradeScore = calculateTradeScore(requestedItem, offeredItems)
// Result: 15 + 5 + 3 + 3 + 2 = 28 points
```

### User Level Progression
```kotlin
// User with 95 points completes trade earning 28 points
// New total: 123 points
// New level: 3 (Novice Trader)
```

## 🔧 Implementation Details

### 1. Trade Completion Flow
```kotlin
fun completeTrade() {
    // 1. Update meetup status
    // 2. Save updated offer
    // 3. Create trade history with calculated scores
    // 4. Update both users' scores and levels
    // 5. Show success message with level progression
}
```

### 2. Score Calculation Integration
- **Real-time Calculation**: Scores calculated during trade completion
- **Persistent Storage**: Scores saved to database immediately
- **User Feedback**: Success messages include level progression
- **Error Handling**: Comprehensive error handling for all operations

### 3. Database Index Creation
- **Automatic Creation**: Indexes created when database connects
- **Performance Monitoring**: Console logging for index creation status
- **Error Handling**: Graceful handling of index creation failures

## 🎯 Benefits

### 1. User Engagement
- **Gamification**: Level progression encourages continued trading
- **Environmental Awareness**: Carbon tracking promotes sustainability
- **Achievement System**: Trade scores provide sense of accomplishment

### 2. Platform Performance
- **Fast Queries**: Optimized database indexes
- **Scalable Architecture**: Efficient data access patterns
- **Data Integrity**: Cleanup processes maintain database health

### 3. Business Value
- **User Retention**: Gamification increases user engagement
- **Environmental Impact**: Carbon tracking aligns with sustainability goals
- **Analytics**: Rich data for business intelligence
