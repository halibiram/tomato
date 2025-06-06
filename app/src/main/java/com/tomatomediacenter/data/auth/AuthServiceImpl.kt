package com.tomatomediacenter.data.auth

import com.tomatomediacenter.domain.auth.AuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthServiceImpl @Inject constructor() : AuthService {

    // Simulate a logged-in state with a Flow
    private val _authState = MutableStateFlow(false) // Initially not authenticated
    override val authState: StateFlow<Boolean> = _authState.asStateFlow()

    private var currentUserId: String? = null

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(1000) // Simulate network delay

        return if (email == "test@example.com" && password == "password") {
            _authState.value = true
            currentUserId = "user_123"
            Result.success(Unit)
        } else {
            _authState.value = false
            currentUserId = null
            Result.failure(Exception("Invalid email or password"))
        }
    }

    override suspend fun logout() {
        delay(500) // Simulate network delay
        _authState.value = false
        currentUserId = null
    }

    override suspend fun register(email: String, password: String, username: String?): Result<Unit> {
        delay(1500) // Simulate network delay

        return if (email.isNotEmpty() && password.isNotEmpty() && !email.contains("existing")) {
            // Simulate successful registration
            // For a real app, you'd store this user and then log them in or require verification.
            // For this dummy impl, we won't automatically log them in.
            Result.success(Unit)
        } else if (email.contains("existing")) {
            Result.failure(Exception("Email already exists"))
        } else {
            Result.failure(Exception("Registration failed due to invalid data"))
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        delay(1000) // Simulate network delay
        return if (email == "test@example.com" || email == "exists@example.com") {
            // Simulate sending a reset link
            Result.success(Unit)
        } else {
            Result.failure(Exception("Email not found"))
        }
    }

    override fun getCurrentUserId(): String? {
        return currentUserId
    }
}
