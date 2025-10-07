package com.example.swoptrader.data.repository

import com.example.swoptrader.data.local.dao.TradeHistoryDao
import com.example.swoptrader.data.model.TradeHistory
import com.example.swoptrader.data.model.toEntity
import com.example.swoptrader.data.model.toTradeHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface TradeHistoryRepository {
    suspend fun createTradeHistory(tradeHistory: TradeHistory): Result<TradeHistory>
    suspend fun getTradeHistory(tradeId: String): Result<TradeHistory?>
    suspend fun getTradeHistoryByUser(userId: String): Result<List<TradeHistory>>
    suspend fun getTradeHistoryByOfferId(offerId: String): Result<TradeHistory?>
    fun getTradeHistoryByUserFlow(userId: String): Flow<List<TradeHistory>>
    suspend fun updateTradeHistory(tradeHistory: TradeHistory): Result<TradeHistory>
    suspend fun syncTradeHistoryWithRemote(userId: String): Result<Unit>
}

@Singleton
class TradeHistoryRepositoryImpl @Inject constructor(
    private val tradeHistoryDao: TradeHistoryDao,
    private val firestoreRepository: FirestoreRepository
) : TradeHistoryRepository {
    
    override suspend fun createTradeHistory(tradeHistory: TradeHistory): Result<TradeHistory> {
        return try {
            // Save to local database first
            tradeHistoryDao.insertTrade(tradeHistory.toEntity())
            
            // Then save to Firestore
            val firestoreResult = firestoreRepository.saveTradeHistory(tradeHistory)
            firestoreResult.fold(
                onSuccess = {
                    Result.success(tradeHistory)
                },
                onFailure = { error ->
                    println("Failed to save trade history to Firestore: ${error.message}")
                    Result.success(tradeHistory)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTradeHistory(tradeId: String): Result<TradeHistory?> {
        return try {
            // First try to get from local database
            val localTrade = tradeHistoryDao.getTradeById(tradeId)
            if (localTrade != null) {
                Result.success(localTrade.toTradeHistory())
            } else {
                // If not found locally, try to sync from Firestore
                val firestoreResult = firestoreRepository.getTradeHistory(tradeId)
                firestoreResult.fold(
                    onSuccess = { tradeHistory ->
                        tradeHistory?.let {
                            // Save to local database
                            tradeHistoryDao.insertTrade(it.toEntity())
                        }
                        Result.success(tradeHistory)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTradeHistoryByUser(userId: String): Result<List<TradeHistory>> {
        return try {
            // First sync with remote to get latest trade history
            syncTradeHistoryWithRemote(userId)
            
            // Then get from local database
            val tradeHistory = tradeHistoryDao.getTradesByUserOnce(userId).map { it.toTradeHistory() }
            Result.success(tradeHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTradeHistoryByOfferId(offerId: String): Result<TradeHistory?> {
        return try {
            // First try to get from local database
            val localTrade = tradeHistoryDao.getTradeByOfferId(offerId)
            if (localTrade != null) {
                Result.success(localTrade.toTradeHistory())
            } else {
                // If not found locally, try to sync from Firestore
                val firestoreResult = firestoreRepository.getTradeHistoryByOfferId(offerId)
                firestoreResult.fold(
                    onSuccess = { tradeHistory ->
                        tradeHistory?.let {
                            // Save to local database
                            tradeHistoryDao.insertTrade(it.toEntity())
                        }
                        Result.success(tradeHistory)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getTradeHistoryByUserFlow(userId: String): Flow<List<TradeHistory>> {
        return tradeHistoryDao.getTradesByUser(userId).map { entities ->
            entities.map { it.toTradeHistory() }
        }
    }
    
    override suspend fun updateTradeHistory(tradeHistory: TradeHistory): Result<TradeHistory> {
        return try {
            // Update local database first
            tradeHistoryDao.updateTrade(tradeHistory.toEntity())
            
            // Then update Firestore
            val firestoreResult = firestoreRepository.saveTradeHistory(tradeHistory)
            firestoreResult.fold(
                onSuccess = {
                    Result.success(tradeHistory)
                },
                onFailure = { error ->
                    println("Failed to update trade history in Firestore: ${error.message}")
                    Result.success(tradeHistory)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncTradeHistoryWithRemote(userId: String): Result<Unit> {
        return try {
            val firestoreResult = firestoreRepository.getTradeHistoryByUser(userId)
            firestoreResult.fold(
                onSuccess = { tradeHistory ->
                    // Save to local database
                    val tradeEntities = tradeHistory.map { it.toEntity() }
                    tradeHistoryDao.insertTrades(tradeEntities)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
