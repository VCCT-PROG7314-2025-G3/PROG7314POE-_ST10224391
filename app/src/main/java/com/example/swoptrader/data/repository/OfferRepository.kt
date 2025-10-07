package com.example.swoptrader.data.repository

import com.example.swoptrader.data.local.dao.OfferDao
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.toEntity
import com.example.swoptrader.data.model.toOffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface OfferRepository {
    suspend fun createOffer(offer: Offer): Result<Offer>
    suspend fun getOfferById(offerId: String): Result<Offer?>
    suspend fun getOffersByUser(userId: String): Result<List<Offer>>
    suspend fun getOffersForItem(itemId: String): Result<List<Offer>>
    suspend fun updateOffer(offer: Offer): Result<Offer>
    suspend fun updateOfferStatus(offerId: String, status: com.example.swoptrader.data.model.OfferStatus): Result<Offer>
    fun getOffersByUserFlow(userId: String): Flow<List<Offer>>
    suspend fun syncOffersWithRemote(userId: String): Result<Unit>
}

@Singleton
class OfferRepositoryImpl @Inject constructor(
    private val offerDao: OfferDao,
    private val firestoreRepository: FirestoreRepository
) : OfferRepository {
    
    override suspend fun createOffer(offer: Offer): Result<Offer> {
        return try {
            // Save to local database first
            offerDao.insertOffer(offer.toEntity())
            
            // Then save to Firestore
            val firestoreResult = firestoreRepository.saveOffer(offer)
            firestoreResult.fold(
                onSuccess = {
                    Result.success(offer)
                },
                onFailure = { error ->
                    println("Failed to save offer to Firestore: ${error.message}")
                    Result.success(offer)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOfferById(offerId: String): Result<Offer?> {
        return try {
            // First try to get from local database
            val localOffer = offerDao.getOfferById(offerId)
            if (localOffer != null) {
                Result.success(localOffer.toOffer())
            } else {
                // If not found locally, try to sync from Firestore
                val firestoreResult = firestoreRepository.getOffer(offerId)
                firestoreResult.fold(
                    onSuccess = { offer ->
                        offer?.let {
                            // Save to local database
                            offerDao.insertOffer(it.toEntity())
                        }
                        Result.success(offer)
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
    
    override suspend fun getOffersByUser(userId: String): Result<List<Offer>> {
        return try {
            // First sync with remote to get latest offers
            syncOffersWithRemote(userId)
            
            // Then get from local database
            val offers = offerDao.getOffersByUserOnce(userId).map { it.toOffer() }
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOffersForItem(itemId: String): Result<List<Offer>> {
        return try {
            // First try to get from local database
            val localOffers = offerDao.getOffersForItem(itemId).map { it.toOffer() }
            Result.success(localOffers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateOffer(offer: Offer): Result<Offer> {
        return try {
            // Update local database first
            offerDao.updateOffer(offer.toEntity())
            
            // Then update Firestore
            val firestoreResult = firestoreRepository.saveOffer(offer)
            firestoreResult.fold(
                onSuccess = {
                    Result.success(offer)
                },
                onFailure = { error ->
                    println("Failed to update offer in Firestore: ${error.message}")
                    Result.success(offer)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateOfferStatus(offerId: String, status: com.example.swoptrader.data.model.OfferStatus): Result<Offer> {
        return try {
            // Get the offer first
            val offerResult = getOfferById(offerId)
            offerResult.fold(
                onSuccess = { offer ->
                    if (offer != null) {
                        val updatedOffer = offer.copy(
                            status = status,
                            updatedAt = System.currentTimeMillis()
                        )
                        updateOffer(updatedOffer)
                    } else {
                        Result.failure(Exception("Offer not found"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getOffersByUserFlow(userId: String): Flow<List<Offer>> {
        return offerDao.getOffersByUser(userId).map { entities ->
            entities.map { it.toOffer() }
        }
    }
    
    override suspend fun syncOffersWithRemote(userId: String): Result<Unit> {
        return try {
            val firestoreResult = firestoreRepository.getOffersByUser(userId)
            firestoreResult.fold(
                onSuccess = { offers ->
                    // Save to local database
                    val offerEntities = offers.map { it.toEntity() }
                    offerDao.insertOffers(offerEntities)
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
