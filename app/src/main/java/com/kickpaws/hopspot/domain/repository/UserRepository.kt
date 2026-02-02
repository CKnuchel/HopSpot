package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.User

interface UserRepository {
    suspend fun getMe(): Result<User>
    suspend fun updateProfile(displayName: String): Result<User>
}