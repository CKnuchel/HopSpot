package com.kickpaws.hopspot.data.remote.interceptor

import com.kickpaws.hopspot.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No token for Auth-Endpoints (Login, Register)
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getAccessTokenOnce() }

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}