package com.kickpaws.hopspot.domain.model

data class InvitationCode(
    val id: Int,
    val code: String,
    val comment: String?,
    val createdBy: User,
    val redeemedBy: User?,
    val createdAt: String,
    val redeemedAt: String?
) {
    val isRedeemed: Boolean get() = redeemedBy != null
}