package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User

interface AdminRepository {
    suspend fun getUsers(page: Int = 1, limit: Int = 50): Result<List<User>>
    suspend fun updateUser(id: Int, role: String?, isActive: Boolean?): Result<User>
    suspend fun deleteUser(id: Int): Result<Unit>

    suspend fun getInvitationCodes(page: Int = 1, limit: Int = 50): Result<List<InvitationCode>>
    suspend fun createInvitationCode(comment: String?): Result<InvitationCode>
    suspend fun deleteInvitationCode(id: Int): Result<Unit>
}