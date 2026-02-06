package com.kickpaws.hopspot.di

import android.content.Context
import androidx.room.Room
import com.kickpaws.hopspot.data.local.HopSpotDatabase
import com.kickpaws.hopspot.data.local.dao.PendingPhotoDao
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.dao.UserDao
import com.kickpaws.hopspot.data.local.dao.VisitDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HopSpotDatabase {
        return Room.databaseBuilder(
            context,
            HopSpotDatabase::class.java,
            "hopspot_database"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideSpotDao(database: HopSpotDatabase): SpotDao {
        return database.spotDao()
    }

    @Provides
    @Singleton
    fun provideVisitDao(database: HopSpotDatabase): VisitDao {
        return database.visitDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: HopSpotDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun providePendingPhotoDao(database: HopSpotDatabase): PendingPhotoDao {
        return database.pendingPhotoDao()
    }
}
