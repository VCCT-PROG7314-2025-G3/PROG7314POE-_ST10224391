package com.example.swoptrader.data.repository

import com.example.swoptrader.data.model.Meetup
import com.example.swoptrader.data.model.MeetupStatus
import javax.inject.Inject

interface MeetupRepository {
    suspend fun createMeetup(meetup: Meetup): Result<Meetup>
    suspend fun getMeetupById(id: String): Result<Meetup?>
    suspend fun getMeetupByOfferId(offerId: String): Result<Meetup?>
    suspend fun updateMeetup(meetup: Meetup): Result<Meetup>
    suspend fun updateMeetupStatus(id: String, status: MeetupStatus): Result<Meetup>
    suspend fun deleteMeetup(id: String): Result<Unit>
}

class MeetupRepositoryImpl @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : MeetupRepository {
    
    override suspend fun createMeetup(meetup: Meetup): Result<Meetup> {
        return firestoreRepository.saveMeetup(meetup)
    }
    
    override suspend fun getMeetupById(id: String): Result<Meetup?> {
        return firestoreRepository.getMeetup(id)
    }
    
    override suspend fun getMeetupByOfferId(offerId: String): Result<Meetup?> {
        return firestoreRepository.getMeetupByOfferId(offerId)
    }
    
    override suspend fun updateMeetup(meetup: Meetup): Result<Meetup> {
        return firestoreRepository.saveMeetup(meetup)
    }
    
    override suspend fun updateMeetupStatus(id: String, status: MeetupStatus): Result<Meetup> {
        return try {
            val meetupResult = getMeetupById(id)
            meetupResult.fold(
                onSuccess = { meetup ->
                    if (meetup != null) {
                        val updatedMeetup = meetup.copy(
                            status = status,
                            updatedAt = System.currentTimeMillis()
                        )
                        firestoreRepository.saveMeetup(updatedMeetup)
                    } else {
                        Result.failure(Exception("Meetup not found"))
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
    
    override suspend fun deleteMeetup(id: String): Result<Unit> {
        return firestoreRepository.deleteMeetup(id)
    }
}


