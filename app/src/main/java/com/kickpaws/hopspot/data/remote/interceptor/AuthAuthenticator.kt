package com.kickpaws.hopspot.data.remote.interceptor

import com.kickpaws.hopspot.data.local.TokenManager
import com.kickpaws.hopspot.data.remote.dto.RefreshTokenRequest
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiProvider: Provider<HopSpotApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization-Retry") != null) {
            return null
        }

        val refreshToken = runBlocking { tokenManager.getRefreshTokenOnce() }
            ?: return null

        return runBlocking {
            try {
                val result = apiProvider.get().refreshToken(
                    RefreshTokenRequest(refreshToken)
                )

                // save new token
                tokenManager.saveTokens(result.token, result.refreshToken)

                // Retry request with new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${result.token}")
                    .header("Authorization-Retry", "true")
                    .build()

            } catch (_: Exception) {
                // Refresh failed → Logout
                tokenManager.clearTokens()
                null
            }
        }
    }
}