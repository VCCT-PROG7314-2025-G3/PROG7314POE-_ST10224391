package com.example.swoptrader.di

import android.content.Context
import androidx.room.Room
import com.example.swoptrader.data.local.SwopTraderDatabase
import com.example.swoptrader.data.local.dao.ChatDao
import com.example.swoptrader.data.local.dao.CommentDao
import com.example.swoptrader.data.local.dao.ItemDao
import com.example.swoptrader.data.local.dao.OfferDao
import com.example.swoptrader.data.local.dao.TradeHistoryDao
import com.example.swoptrader.data.local.dao.UserDao
import com.example.swoptrader.data.repository.UserRepository
import com.example.swoptrader.data.repository.UserRepositoryImpl
import com.example.swoptrader.data.repository.MeetupRepository
import com.example.swoptrader.data.repository.MeetupRepositoryImpl
import com.example.swoptrader.data.repository.FirestoreRepository
import com.example.swoptrader.service.LocationService
import com.example.swoptrader.service.PlacesService
import com.example.swoptrader.service.TranslationService
import com.example.swoptrader.service.TranslationStateManager
import com.example.swoptrader.service.TranslationManager
import com.example.swoptrader.service.GoogleSignInService
import com.example.swoptrader.service.SessionManager
import com.example.swoptrader.service.GeocodingService
import com.example.swoptrader.ui.screens.settings.SettingsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSwopTraderDatabase(
        @ApplicationContext context: Context
    ): SwopTraderDatabase {
        return Room.databaseBuilder(
            context,
            SwopTraderDatabase::class.java,
            "swoptrader_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: SwopTraderDatabase): UserDao = database.userDao()

    @Provides
    fun provideItemDao(database: SwopTraderDatabase): ItemDao = database.itemDao()
    
    @Provides
    fun provideCommentDao(database: SwopTraderDatabase): com.example.swoptrader.data.local.dao.CommentDao = database.commentDao()
    
    @Provides
    fun provideChatDao(database: SwopTraderDatabase): ChatDao = database.chatDao()
    
    @Provides
    fun provideOfferDao(database: SwopTraderDatabase): OfferDao = database.offerDao()
    
    @Provides
    fun provideTradeHistoryDao(database: SwopTraderDatabase): TradeHistoryDao = database.tradeHistoryDao()
    
    @Provides
    @Singleton
    fun provideMeetupRepository(firestoreRepository: FirestoreRepository): MeetupRepository = MeetupRepositoryImpl(firestoreRepository)
    
    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context): LocationService = LocationService(context)
    
    @Provides
    @Singleton
    fun providePlacesService(@ApplicationContext context: Context): PlacesService = PlacesService(context)
    
    @Provides
    @Singleton
    fun provideTranslationService(@ApplicationContext context: Context): TranslationService = TranslationService(context)
    
    @Provides
    @Singleton
    fun provideTranslationStateManager(): TranslationStateManager = TranslationStateManager()
    
    @Provides
    @Singleton
    fun provideTranslationManager(): TranslationManager = TranslationManager()
    
    @Provides
    @Singleton
    fun provideGoogleSignInService(@ApplicationContext context: Context): GoogleSignInService = GoogleSignInService(context)
    
    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager = SessionManager(context)
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideGeocodingService(@ApplicationContext context: Context): GeocodingService = GeocodingService(context)
    
    @Provides
    @Singleton
    fun provideLocationPermissionService(@ApplicationContext context: Context): com.example.swoptrader.service.LocationPermissionService = com.example.swoptrader.service.LocationPermissionService(context)
    
    @Provides
    @Singleton
    fun provideImagePickerService(@ApplicationContext context: Context): com.example.swoptrader.service.ImagePickerService = com.example.swoptrader.service.ImagePickerService(context)
    
    @Provides
    @Singleton
    fun provideBiometricAuthService(@ApplicationContext context: Context): com.example.swoptrader.service.BiometricAuthService = com.example.swoptrader.service.BiometricAuthService(context)
    
    @Provides
    @Singleton
    fun provideDistanceUpdateService(): com.example.swoptrader.service.DistanceUpdateService = com.example.swoptrader.service.DistanceUpdateService()
    
    @Provides
    @Singleton
    fun provideFirebaseStorageService(): com.example.swoptrader.service.FirebaseStorageService = com.example.swoptrader.service.FirebaseStorageService()
}