package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.data.local.CurrentUserManager
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.UpdateProfileRequest
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: HopSpotApi,
    private val currentUserManager: CurrentUserManager
) : UserRepository {

    override suspend fun getMe(): Result<User> {
        return try {
            val response = api.getMe()
            val user = response.toDomain()
            currentUserManager.setUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun updateProfile(displayName: String): Result<User> {
        return try {
            val response = api.updateProfile(UpdateProfileRequest(displayName = displayName))
            val user = response.toDomain()
            currentUserManager.setUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }
}
