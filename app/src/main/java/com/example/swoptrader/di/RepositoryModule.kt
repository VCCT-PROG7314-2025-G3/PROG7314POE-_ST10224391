package com.example.swoptrader.di

import com.example.swoptrader.data.local.dao.ChatDao
import com.example.swoptrader.data.local.dao.ItemDao
import com.example.swoptrader.data.local.dao.OfferDao
import com.example.swoptrader.data.local.dao.TradeHistoryDao
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.data.repository.TradeHistoryRepository
import com.example.swoptrader.data.repository.AuthRepositoryImpl
import com.example.swoptrader.data.repository.ChatRepositoryImpl
import com.example.swoptrader.data.repository.ItemRepositoryImpl
import com.example.swoptrader.data.repository.OfferRepositoryImpl
import com.example.swoptrader.data.repository.TradeHistoryRepositoryImpl
import com.example.swoptrader.data.repository.FirestoreRepository
import com.example.swoptrader.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(
        api: com.example.swoptrader.data.remote.api.SwopTraderApi,
        firestoreRepository: FirestoreRepository
    ): com.example.swoptrader.data.repository.UserRepository {
        return com.example.swoptrader.data.repository.UserRepositoryImpl(api, firestoreRepository)
    }
    
    @Provides
    @Singleton
    fun provideCommentRepository(
        commentDao: com.example.swoptrader.data.local.dao.CommentDao,
        firestoreRepository: FirestoreRepository
    ): com.example.swoptrader.data.repository.CommentRepository {
        return com.example.swoptrader.data.repository.CommentRepositoryImpl(commentDao, firestoreRepository)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: com.example.swoptrader.data.remote.api.SwopTraderApi,
        firestoreRepository: FirestoreRepository,
        geocodingService: com.example.swoptrader.service.GeocodingService,
        locationPermissionService: com.example.swoptrader.service.LocationPermissionService,
        userRepository: com.example.swoptrader.data.repository.UserRepository
    ): AuthRepository {
        return AuthRepositoryImpl(api, firestoreRepository, geocodingService, locationPermissionService, userRepository)
    }
    
    @Provides
    @Singleton
    fun provideFirestoreRepository(
        firestore: com.google.firebase.firestore.FirebaseFirestore
    ): FirestoreRepository {
        return FirestoreRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideSyncManager(
        firestoreRepository: FirestoreRepository,
        itemDao: ItemDao
    ): SyncManager {
        return SyncManager(firestoreRepository, itemDao)
    }
    
    @Provides
    @Singleton
    fun provideItemRepository(
        api: com.example.swoptrader.data.remote.api.SwopTraderApi,
        itemDao: ItemDao,
        syncManager: SyncManager
    ): ItemRepository {
        return ItemRepositoryImpl(api, itemDao, syncManager)
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        firestoreRepository: FirestoreRepository
    ): ChatRepository {
        return ChatRepositoryImpl(chatDao, firestoreRepository)
    }
    
    @Provides
    @Singleton
    fun provideOfferRepository(
        offerDao: OfferDao,
        firestoreRepository: FirestoreRepository
    ): OfferRepository {
        return OfferRepositoryImpl(offerDao, firestoreRepository)
    }
    
    @Provides
    @Singleton
    fun provideTradeHistoryRepository(
        tradeHistoryDao: TradeHistoryDao,
        firestoreRepository: FirestoreRepository
    ): TradeHistoryRepository {
        return TradeHistoryRepositoryImpl(tradeHistoryDao, firestoreRepository)
    }
}
