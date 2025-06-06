package com.tomatomediacenter.domain.models

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This file defines a standardized set of custom error types (`TomatoError`) for the
 *    application. Using a sealed class hierarchy for errors allows for more specific error
 *    handling and better communication of what went wrong throughout the app layers.
 *    The `ErrorHandler` object provides a centralized utility to convert these specific
 *    errors into user-friendly messages.
 *
 * 2. How developers should use it:
 *    - `TomatoError`: When a known type of error occurs (e.g., network timeout, database
 *      insertion failure), throw or return an instance of the appropriate `TomatoError` subclass.
 *      For example, a repository might catch a `SocketTimeoutException` and rethrow it as
 *      `TomatoError.TimeoutError("Network request timed out")`.
 *    - `ErrorHandler.getMessage(throwable: Throwable)`: In the UI layer (e.g., ViewModel or
 *      sometimes directly in Composables if simple), use this function to convert any caught
 *      `Throwable` into a human-readable string suitable for display to the user. It provides
 *      default messages for known `TomatoError` types and a generic message for unknown errors.
 *
 * 3. What NOT to do:
 *    - Do not rely solely on generic `Exception` types. Use specific `TomatoError` types
 *      wherever possible to provide more context.
 *    - Avoid creating user-facing error messages directly in lower layers like data or domain.
 *      The domain/data layers should propagate structured errors (`TomatoError`), and the
 *      presentation layer (using `ErrorHandler`) should be responsible for localization and
 *      user-friendly messages.
 *    - Don't add UI-specific error handling logic (e.g., showing a Dialog) into `ErrorHandler`.
 *      It should only be responsible for message transformation.
 *
 * 4. Common pitfalls to avoid:
 *    - Overly broad `TomatoError` types that don't offer much more information than a
 *      generic exception. Strive for a good balance of specificity.
 *    - Forgetting to wrap underlying exceptions if they provide useful context, or conversely,
 *      leaking sensitive details from low-level exceptions into user messages.
 *    - Inconsistent use of `ErrorHandler`, leading to different error message styles across
 *      the app.
 *
 * 5. Integration with other components:
 *    - `TomatoError` instances are typically created in the data or domain layers (Repositories,
 *      UseCases) when operations fail.
 *    - `Result.failure(TomatoError(...))` is a common pattern in UseCases and Repositories.
 *    - ViewModels (`BaseViewModel`) catch exceptions or receive `Result.failure` from UseCases,
 *      and can use `ErrorHandler.getMessage()` to update an error message `StateFlow`
 *      that the UI observes. (Note: Corrected from `ErrorHandler.handleError()` to `ErrorHandler.getMessage()` as per the code)
 *
 * 6. Testing strategies:
 *    - Test that different system exceptions are correctly mapped to `TomatoError` types
 *      in repositories or services.
 *    - Test the `ErrorHandler.getMessage()` function to ensure it returns the expected
 *      user-friendly messages for each `TomatoError` type and for generic exceptions.
 *    - In ViewModel tests, verify that appropriate error messages (derived via `ErrorHandler`)
 *      are set when use cases return failures.
 */

/**
 * Base sealed class for custom application-specific errors.
 * This allows for exhaustive checks when handling errors.
 */
sealed class TomatoError : Exception() {

    // --- Network Errors ---
    /** Indicates a general network connectivity issue. */
    data class NetworkError(override val message: String = "Could not connect to the server.") : TomatoError()

    /** Indicates that a network request timed out. */
    data class TimeoutError(override val message: String = "The request timed out. Please try again.") : TomatoError()

    /** Indicates an error reported by the server (e.g., HTTP 500, 403). */
    data class ServerError(val code: Int, override val message: String) : TomatoError()

    // --- Database Errors ---
    /** Indicates a general issue with the local database. */
    data class DatabaseError(override val message: String = "A local data storage error occurred.") : TomatoError()

    /** Indicates an issue specifically with caching data (could be read or write). */
    data class CacheError(override val message: String = "There was a problem with the local cache.") : TomatoError()

    // --- Business Logic Errors ---
    /** Indicates that input data failed validation. */
    data class ValidationError(override val message: String) : TomatoError() // Message should be specific

    /** Indicates an authentication failure (e.g., invalid credentials, expired token). */
    data class AuthenticationError(override val message: String = "Authentication failed.") : TomatoError()

    /** Indicates that the user does not have permission to perform an action. */
    data class PermissionError(override val message: String = "You don't have permission to do this.") : TomatoError()

    // --- Extension System Errors ---
    /** Indicates a general error originating from an extension. */
    data class ExtensionError(val extensionName: String? = null, override val message: String) : TomatoError()

    /** Indicates that a specific extension could not be found or loaded. */
    data class ExtensionNotFoundError(val extensionId: String) :
        TomatoError() {
        override val message: String = "The extension with ID '$extensionId' could not be found."
    }

    // --- Download Manager Errors ---
    /** Indicates a general error during the download process. */
    data class DownloadError(val fileName: String? = null, override val message: String) : TomatoError()

    /** Indicates an issue with device storage (e.g., not enough space). */
    data class StorageError(override val message: String = "Not enough storage space, or storage is unavailable.") : TomatoError()

    // --- Media Player Errors ---
    /** Indicates a general error related to media playback. */
    data class PlayerError(override val message: String) : TomatoError()

    /** Indicates an error related to media codecs or unsupported formats. */
    data class CodecError(override val message: String = "The media format is unsupported or corrupted.") : TomatoError()

    // --- Unknown or Generic Error ---
    /** For errors that don't fit into other categories or when mapping from an unknown exception. */
    data class UnknownError(override val message: String = "An unexpected error occurred.", val originalException: Throwable? = null) : TomatoError()
}

/**
 * Centralized error handler utility.
 * Provides functions to convert Throwables (especially TomatoError types)
 * into user-friendly messages.
 */
object ErrorHandler {

    /**
     * Converts a Throwable into a user-friendly error message.
     * Handles specific `TomatoError` types and provides a generic message for others.
     *
     * @param throwable The error/exception that occurred.
     * @return A string message suitable for display to the user.
     */
    fun getMessage(throwable: Throwable): String {
        return when (throwable) {
            // TomatoError types (already have user-friendly messages or structured info)
            is TomatoError.NetworkError -> throwable.message
            is TomatoError.TimeoutError -> throwable.message
            is TomatoError.ServerError -> "Server error ${throwable.code}: ${throwable.message}"
            is TomatoError.DatabaseError -> throwable.message
            is TomatoError.CacheError -> throwable.message
            is TomatoError.ValidationError -> "Validation failed: ${throwable.message}" // Or just throwable.message
            is TomatoError.AuthenticationError -> throwable.message
            is TomatoError.PermissionError -> throwable.message
            is TomatoError.ExtensionError -> {
                val prefix = throwable.extensionName?.let { "Extension '$it' error: " } ?: "Extension error: "
                prefix + throwable.message
            }
            is TomatoError.ExtensionNotFoundError -> throwable.message
            is TomatoError.DownloadError -> {
                val prefix = throwable.fileName?.let { "Download error for '$it': " } ?: "Download error: "
                prefix + throwable.message
            }
            is TomatoError.StorageError -> throwable.message
            is TomatoError.PlayerError -> "Media player error: ${throwable.message}"
            is TomatoError.CodecError -> "Media codec error: ${throwable.message}"
            is TomatoError.UnknownError -> throwable.message

            // Common platform exceptions (can be mapped to user-friendly messages)
            is java.net.UnknownHostException -> "Cannot reach the server. Please check your internet connection."
            is java.net.SocketTimeoutException -> "The connection timed out. Please try again."
            is java.io.IOException -> "A network or I/O error occurred. Please try again." // Generic IO

            // Fallback for any other Throwables
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Logs the error for debugging purposes.
     * In a real app, this would integrate with a logging library (Timber, Crashlytics, etc.).
     *
     * @param throwable The error/exception to log.
     * @param tag Optional tag for logging.
     */
    fun logError(throwable: Throwable, tag: String = "ErrorHandler") {
        // In a real app, use Timber, Crashlytics, Sentry, etc.
        // e.g., Timber.tag(tag).e(throwable)
        // For now, just print to stderr
        System.err.println("[$tag] Error: ${throwable.javaClass.simpleName} - ${throwable.message}")
        throwable.printStackTrace(System.err) // Print stack trace for debugging
    }
}
