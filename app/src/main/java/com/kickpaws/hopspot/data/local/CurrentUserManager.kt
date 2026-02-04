package com.kickpaws.hopspot.data.local

import com.kickpaws.hopspot.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentUserManager @Inject constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isAdmin: Boolean
        get() = _currentUser.value?.role == "admin"

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
    }
}