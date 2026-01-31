package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        invitationCode: String
    ): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun isLoggedIn(): Boolean
}