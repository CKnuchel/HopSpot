package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.data.local.CurrentUserManager
import com.kickpaws.hopspot.data.local.TokenManager
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.LoginRequest
import com.kickpaws.hopspot.data.remote.dto.LogoutRequest
import com.kickpaws.hopspot.data.remote.dto.RegisterRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: HopSpotApi,
    private val tokenManager: TokenManager,
    private val currentUserManager: CurrentUserManager  // NEU
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            tokenManager.saveTokens(response.token, response.refreshToken)
            val user = response.user.toDomain()
            currentUserManager.setUser(user)  // NEU: Cache user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        invitationCode: String
    ): Result<User> {
        return try {
            val response = api.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    displayName = displayName,
                    invitationCode = invitationCode
                )
            )
            tokenManager.saveTokens(response.token, response.refreshToken)
            val user = response.user.toDomain()
            currentUserManager.setUser(user)  // NEU: Cache user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshTokenOnce()
            if (refreshToken != null) {
                api.logout(LogoutRequest(refreshToken))
            }
            tokenManager.clearTokens()
            currentUserManager.clear()  // NEU: Clear cache
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            currentUserManager.clear()  // NEU: Clear cache
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}