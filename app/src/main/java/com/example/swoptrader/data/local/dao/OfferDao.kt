package com.example.swoptrader.data.local.dao

import androidx.room.*
import com.example.swoptrader.data.model.OfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE id = :offerId")
    suspend fun getOfferById(offerId: String): OfferEntity?

    @Query("SELECT * FROM offers WHERE fromUserId = :userId OR toUserId = :userId ORDER BY createdAt DESC")
    fun getOffersByUser(userId: String): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE fromUserId = :userId OR toUserId = :userId ORDER BY createdAt DESC")
    suspend fun getOffersByUserOnce(userId: String): List<OfferEntity>

    @Query("SELECT * FROM offers WHERE requestedItemId = :itemId OR offeredItemIds LIKE '%' || :itemId || '%' ORDER BY createdAt DESC")
    suspend fun getOffersForItem(itemId: String): List<OfferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)

    @Update
    suspend fun updateOffer(offer: OfferEntity)

    @Delete
    suspend fun deleteOffer(offer: OfferEntity)

    @Query("DELETE FROM offers WHERE fromUserId = :userId OR toUserId = :userId")
    suspend fun deleteOffersByUser(userId: String)

    @Query("DELETE FROM offers")
    suspend fun deleteAllOffers()
}