package com.kickpaws.hopspot.di

import com.kickpaws.hopspot.data.repository.AdminRepositoryImpl
import com.kickpaws.hopspot.data.repository.FavoriteRepositoryImpl
import com.kickpaws.hopspot.data.repository.OfflineFirstBenchRepository
import com.kickpaws.hopspot.data.repository.OfflineFirstVisitRepository
import com.kickpaws.hopspot.data.repository.PhotoRepositoryImpl
import com.kickpaws.hopspot.domain.repository.AdminRepository
import com.kickpaws.hopspot.domain.repository.AuthRepository
import com.kickpaws.hopspot.domain.repository.AuthRepositoryImpl
import com.kickpaws.hopspot.domain.repository.BenchRepository
import com.kickpaws.hopspot.domain.repository.FavoriteRepository
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import com.kickpaws.hopspot.domain.repository.UserRepository
import com.kickpaws.hopspot.domain.repository.UserRepositoryImpl
import com.kickpaws.hopspot.domain.repository.VisitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindBenchRepository(
        offlineFirstBenchRepository: OfflineFirstBenchRepository
    ): BenchRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindVisitRepository(
        offlineFirstVisitRepository: OfflineFirstVisitRepository
    ): VisitRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
}