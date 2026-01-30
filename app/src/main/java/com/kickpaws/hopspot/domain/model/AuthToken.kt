package com.kickpaws.hopspot.domain.model

data class AuthToken(
    val token: String,
    val refreshToken: String
)