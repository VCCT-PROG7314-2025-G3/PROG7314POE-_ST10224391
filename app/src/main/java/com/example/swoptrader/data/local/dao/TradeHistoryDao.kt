package com.example.swoptrader.data.local.dao

import androidx.room.*
import com.example.swoptrader.data.model.TradeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeHistoryDao {
    @Query("SELECT * FROM trade_history WHERE id = :tradeId")
    suspend fun getTradeById(tradeId: String): TradeHistoryEntity?

    @Query("SELECT * FROM trade_history WHERE participantIds LIKE '%' || :userId || '%' ORDER BY completedAt DESC")
    fun getTradesByUser(userId: String): Flow<List<TradeHistoryEntity>>

    @Query("SELECT * FROM trade_history WHERE participantIds LIKE '%' || :userId || '%' ORDER BY completedAt DESC")
    suspend fun getTradesByUserOnce(userId: String): List<TradeHistoryEntity>

    @Query("SELECT * FROM trade_history WHERE offerId = :offerId")
    suspend fun getTradeByOfferId(offerId: String): TradeHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeHistoryEntity>)

    @Update
    suspend fun updateTrade(trade: TradeHistoryEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeHistoryEntity)

    @Query("DELETE FROM trade_history WHERE participantIds LIKE '%' || :userId || '%'")
    suspend fun deleteTradesByUser(userId: String)

    @Query("DELETE FROM trade_history")
    suspend fun deleteAllTrades()
}

