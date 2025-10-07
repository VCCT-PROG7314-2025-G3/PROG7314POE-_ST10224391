package com.example.swoptrader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.swoptrader.data.local.dao.ChatDao
import com.example.swoptrader.data.local.dao.CommentDao
import com.example.swoptrader.data.local.dao.ItemDao
import com.example.swoptrader.data.local.dao.OfferDao
import com.example.swoptrader.data.local.dao.TradeHistoryDao
import com.example.swoptrader.data.local.dao.UserDao
import com.example.swoptrader.data.model.ChatEntity
import com.example.swoptrader.data.model.ChatMessageEntity
import com.example.swoptrader.data.model.CommentEntity
import com.example.swoptrader.data.model.ItemEntity
import com.example.swoptrader.data.model.OfferEntity
import com.example.swoptrader.data.model.TradeHistoryEntity
import com.example.swoptrader.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class,
        CommentEntity::class,
        ChatEntity::class,
        ChatMessageEntity::class,
        OfferEntity::class,
        TradeHistoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class SwopTraderDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun commentDao(): CommentDao
    abstract fun chatDao(): ChatDao
    abstract fun offerDao(): OfferDao
    abstract fun tradeHistoryDao(): TradeHistoryDao
}