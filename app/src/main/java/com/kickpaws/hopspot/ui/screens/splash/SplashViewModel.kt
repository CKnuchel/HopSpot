package com.kickpaws.hopspot.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kickpaws.hopspot.data.local.CurrentUserManager
import com.kickpaws.hopspot.data.local.TokenManager
import com.kickpaws.hopspot.data.local.dao.UserDao
import com.kickpaws.hopspot.data.local.mapper.toDomain
import com.kickpaws.hopspot.data.local.mapper.toEntity
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.RefreshFCMTokenRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: HopSpotApi,
    private val currentUserManager: CurrentUserManager,
    private val networkMonitor: NetworkMonitor,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val hasToken = tokenManager.isLoggedIn()
            val isOnline = networkMonitor.isOnlineNow
            val cachedUser = userDao.getCurrentUser()

            when {
                hasToken && isOnline -> {
                    // Online with token: validate via API
                    try {
                        val user = api.getMe().toDomain()
                        currentUserManager.setUser(user)

                        // Cache user for offline use
                        userDao.insertUser(user.toEntity())

                        sendFcmTokenToBackend()

                        _uiState.update {
                            it.copy(isLoading = false, isLoggedIn = true, isOffline = false)
                        }
                    } catch (e: Exception) {
                        // API failed, check if we have cached user
                        if (cachedUser != null) {
                            // Use cached user in offline mode
                            currentUserManager.setUser(cachedUser.toDomain())
                            _uiState.update {
                                it.copy(isLoading = false, isLoggedIn = true, isOffline = true)
                            }
                        } else {
                            // No cached user, clear tokens and go to login
                            tokenManager.clearTokens()
                            _uiState.update {
                                it.copy(isLoading = false, isLoggedIn = false, isOffline = false)
                            }
                        }
                    }
                }

                hasToken && !isOnline && cachedUser != null -> {
                    // Offline mode with cached user
                    currentUserManager.setUser(cachedUser.toDomain())
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, isOffline = true)
                    }
                }

                hasToken && !isOnline && cachedUser == null -> {
                    // First start without internet - need internet for first login
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            isOffline = true,
                            error = "Internet fuer erste Anmeldung erforderlich"
                        )
                    }
                }

                else -> {
                    // No token, go to login
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = false, isOffline = !isOnline)
                    }
                }
            }
        }
    }

    private suspend fun sendFcmTokenToBackend() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            api.refreshFcmToken(RefreshFCMTokenRequest(token))
        } catch (e: Exception) {
            // Not critical - will retry on next app start
        }
    }
}
