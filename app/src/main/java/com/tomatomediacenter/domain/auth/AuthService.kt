package com.tomatomediacenter.domain.auth

import kotlinx.coroutines.flow.Flow

/**
 * Interface for authentication-related operations.
 * Defines the contract for user authentication functionalities like login, logout,
 * registration, and session management.
 */
interface AuthService {

    /**
     * Represents the current authentication state of the user.
     * Emits true if the user is authenticated, false otherwise.
     * Can also emit specific AuthState objects for more granular states (e.g., Uninitialized, Authenticated, Unauthenticated).
     */
    val authState: Flow<Boolean> // Or Flow<AuthState> for more detailed states

    /**
     * Attempts to log in a user with the given credentials.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return A Result object indicating success or failure. Failure may contain an error message or exception.
     */
    suspend fun login(email: String, password: String): Result<Unit>

    /**
     * Logs out the currently authenticated user.
     */
    suspend fun logout()

    /**
     * Attempts to register a new user with the given details.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param username Optional username for the user.
     * @return A Result object indicating success or failure.
     */
    suspend fun register(email: String, password: String, username: String? = null): Result<Unit>

    /**
     * Sends a password reset link or code to the user's email.
     *
     * @param email The email address to send the reset link to.
     * @return A Result object indicating success or failure.
     */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /**
     * Optionally, provide a way to get the current user's ID or basic profile.
     * This might return null if no user is authenticated.
     */
    fun getCurrentUserId(): String?

    // Example of a more detailed AuthState if needed:
    // sealed class AuthState {
    //     object Uninitialized : AuthState()
    //     data class Authenticated(val userId: String, val email: String?) : AuthState()
    //     object Unauthenticated : AuthState()
    // }
}
