package com.olapp.di

import android.content.Context
import androidx.room.Room
import com.olapp.data.local.OlaDatabase
import com.olapp.data.local.dao.MatchDao
import com.olapp.data.local.dao.ReceivedOlaDao
import com.olapp.data.local.dao.SentOlaDao
import com.olapp.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OlaDatabase =
        Room.databaseBuilder(context, OlaDatabase::class.java, "ola_database")
            .addMigrations(OlaDatabase.MIGRATION_2_3, OlaDatabase.MIGRATION_3_4, OlaDatabase.MIGRATION_4_5, OlaDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserProfileDao(db: OlaDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideReceivedOlaDao(db: OlaDatabase): ReceivedOlaDao = db.receivedOlaDao()
    @Provides fun provideSentOlaDao(db: OlaDatabase): SentOlaDao = db.sentOlaDao()
    @Provides fun provideMatchDao(db: OlaDatabase): MatchDao = db.matchDao()
}
