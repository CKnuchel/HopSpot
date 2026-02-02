package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.UpdateProfileRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : UserRepository {

    override suspend fun getMe(): Result<User> {
        return try {
            val response = api.getMe()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(displayName: String): Result<User> {
        return try {
            val response = api.updateProfile(UpdateProfileRequest(displayName = displayName))
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}