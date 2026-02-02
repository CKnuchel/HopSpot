package com.kickpaws.hopspot.di

import com.kickpaws.hopspot.data.repository.AuthRepositoryImpl
import com.kickpaws.hopspot.data.repository.BenchRepositoryImpl
import com.kickpaws.hopspot.data.repository.PhotoRepositoryImpl
import com.kickpaws.hopspot.domain.repository.AuthRepository
import com.kickpaws.hopspot.domain.repository.BenchRepository
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import com.kickpaws.hopspot.domain.repository.UserRepository
import com.kickpaws.hopspot.domain.repository.UserRepositoryImpl
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
        // Defines the object to inject for the called interface
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
        benchRepositoryImpl: BenchRepositoryImpl
    ): BenchRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository
}