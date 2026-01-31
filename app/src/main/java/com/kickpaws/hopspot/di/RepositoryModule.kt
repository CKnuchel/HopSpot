package com.kickpaws.hopspot.di

import com.kickpaws.hopspot.data.repository.AuthRepositoryImpl
import com.kickpaws.hopspot.domain.repository.AuthRepository
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
}