// data/repository/AdminRepositoryImpl.kt
package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.mapper.toDomain
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.AdminUpdateUserRequest
import com.kickpaws.hopspot.data.remote.dto.CreateInvitationCodeRequest
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User
import com.kickpaws.hopspot.domain.repository.AdminRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : AdminRepository {

    override suspend fun getUsers(page: Int, limit: Int): Result<List<User>> {
        return try {
            val response = api.getUsers(page = page, limit = limit)
            Result.success(response.users.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun updateUser(id: Int, role: String?, isActive: Boolean?): Result<User> {
        return try {
            val request = AdminUpdateUserRequest(role = role, isActive = isActive)
            val response = api.updateUser(id, request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun deleteUser(id: Int): Result<Unit> {
        return try {
            api.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun getInvitationCodes(page: Int, limit: Int): Result<List<InvitationCode>> {
        return try {
            val response = api.getInvitationCodes(page = page, limit = limit)
            Result.success(response.codes.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun createInvitationCode(comment: String?): Result<InvitationCode> {
        return try {
            val request = CreateInvitationCodeRequest(comment = comment)
            val response = api.createInvitationCode(request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun deleteInvitationCode(id: Int): Result<Unit> {
        return try {
            val response = api.deleteInvitationCode(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ApiErrorParser.parse(Exception("Failed to delete invitation code")))
            }
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }
}
