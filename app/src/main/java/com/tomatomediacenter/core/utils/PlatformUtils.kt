package com.tomatomediacenter.core.utils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This file provides a collection of static utility functions related to platform specifics
 *    (`PlatformUtils`) and resource formatting (`ResourceUtils`). Centralizing these utilities
 *    makes them easily accessible and reusable throughout the application, promoting consistency
 *    and reducing code duplication for common tasks like checking device type, formatting file sizes,
 *    or formatting durations.
 *
 * 2. How developers should use it:
 *    - `PlatformUtils`: Call these functions to get information about the current device or
 *      Android version (e.g., `PlatformUtils.isTablet(context)`, `PlatformUtils.isAndroidTV(context)`).
 *    - `ResourceUtils`: Use these functions for common resource formatting tasks:
 *        - `getStringResource(context, resId, *args)`: To get a formatted string from XML resources.
 *        - `formatFileSize(bytes: Long)`: To convert byte counts into human-readable strings (KB, MB, GB).
 *        - `formatDuration(milliseconds: Long)`: To convert milliseconds into a time string (HH:MM:SS or MM:SS).
 *
 * 3. What NOT to do:
 *    - Avoid adding highly specific, feature-related utility functions here. This file should
 *      contain broadly applicable, platform-level or general resource utilities.
 *    - Do not include utilities that require complex dependencies or state management. These
 *      should remain simple, stateless helper functions.
 *    - Don't replicate functionality already easily available through Kotlin standard library or
 *      Android KTX libraries unless there's a clear simplification or specific formatting need.
 *
 * 4. Common pitfalls to avoid:
 *    - NullPointerExceptions if `Context` is not properly provided to functions that require it.
 *    - Incorrectly interpreting device characteristics (e.g., relying on screen size alone for
 *      tablet detection without considering density or configuration). The provided `isTablet`
 *      uses `smallestScreenWidthDp` which is generally reliable.
 *    - Off-by-one errors or incorrect unit conversions in formatting functions.
 *
 * 5. Integration with other components:
 *    - These utility functions can be called from any part of the application (ViewModels,
 *      Composables, Services, etc.) where their functionality is needed.
 *    - Functions requiring `Context` will need it to be passed, often available from
 *      Composables (`LocalContext.current`) or via Hilt injection (`@ApplicationContext`).
 *
 * 6. Testing strategies:
 *    - For functions requiring `Context`, use a mocked `Context` in unit tests (e.g., via Robolectric
 *      or `mockk`).
 *    - Test formatting functions (`formatFileSize`, `formatDuration`) with a variety of inputs,
 *      including edge cases (zero, small values, large values) to ensure correct output.
 *    - For `PlatformUtils`, if possible, run tests on different device configurations or use
 *      Robolectric to simulate them.
 */
object PlatformUtils {

    /**
     * Checks if the current device is considered a tablet.
     * A common definition is a smallest screen width of 600dp or more.
     *
     * @param context Context to access resources and configuration.
     * @return True if the device is considered a tablet, false otherwise.
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    /**
     * Checks if the current device is an Android TV.
     *
     * @param context Context to access PackageManager.
     * @return True if the device is an Android TV, false otherwise.
     */
    fun isAndroidTV(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true
        }
        // Some TVs might not report UI_MODE_TYPE_TELEVISION, check for leanback feature as a fallback
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
               context.packageManager.hasSystemFeature("android.software.leanback") // Alternative leanback feature string
    }

    /**
     * Checks if the device supports High Dynamic Range (HDR) capabilities for display.
     * This is a simplified check based on SDK version. Actual HDR support might also
     * depend on hardware capabilities and display settings.
     *
     * @return True if the Android version is Nougat (API 24) or higher,
     *         which introduced platform HDR support.
     */
    fun supportsHDR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    /**
     * Checks if the device supports Google Cast (Chromecast) functionality.
     * This is a simplified check based on SDK version. Actual Cast support also requires
     * Google Play Services to be available and updated.
     *
     * @return True if the Android version is Lollipop (API 21) or higher.
     */
    fun supportsCast(): Boolean {
        // Basic platform support for Cast APIs started around Lollipop.
        // Actual casting ability also depends on Google Play Services and network.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    /**
     * Gets the Android API level (SDK version) of the current device.
     * e.g., 21 for Lollipop, 30 for Android 11.
     */
    val sdkVersion: Int
        get() = Build.VERSION.SDK_INT

    /**
     * Gets the human-readable Android version string.
     * e.g., "11" for Android 11, "12" for Android 12.
     */
    val androidVersion: String
        get() = Build.VERSION.RELEASE
}

/**
 * Utility functions for handling and formatting resources.
 */
object ResourceUtils {

    /**
     * Retrieves a formatted string resource.
     *
     * @param context Context to access string resources.
     * @param resId The R.string.* ID of the string resource.
     * @param args Variable number of arguments to be formatted into the string.
     * @return The formatted string.
     */
    fun getStringResource(context: Context, resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }

    /**
     * Formats a file size in bytes into a human-readable string (B, KB, MB, GB, TB).
     *
     * @param bytes The file size in bytes.
     * @return A human-readable string representation of the file size.
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 0) return "Invalid size"
        if (bytes < 1024) return "$bytes B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB") // Added Petabytes, Exabytes
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }
        // Format to one decimal place, unless it's Bytes.
        return if (unitIndex == 0) {
            String.format("%.0f %s", size, units[unitIndex])
        } else {
            String.format("%.1f %s", size, units[unitIndex])
        }
    }

    /**
     * Formats a duration in milliseconds into a human-readable time string (HH:MM:SS or MM:SS).
     *
     * @param milliseconds The duration in milliseconds.
     * @return A formatted time string.
     */
    fun formatDuration(milliseconds: Long): String {
        if (milliseconds < 0) return "Invalid duration"

        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds) // Ensure minutes also has leading zero if hours are absent
            else -> String.format(Locale.getDefault(), "0:%02d", seconds) // Show 0:SS for durations less than a minute
        }
    }

    /**
     * Formats a timestamp (in milliseconds since epoch) into a human-readable date string.
     * Example: "Jan 1, 2023" or "10:30 AM" depending on requirements.
     * This is a basic example; more complex formatting might be needed.
     *
     * @param timestampMillis The timestamp in milliseconds.
     * @param pattern The SimpleDateFormat pattern (e.g., "MMM d, yyyy", "HH:mm").
     * @param timeZone The TimeZone to use. Defaults to system default.
     * @return A formatted date string.
     */
    fun formatDate(
        timestampMillis: Long,
        pattern: String = "MMM d, yyyy HH:mm", // Default pattern
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        if (timestampMillis < 0) return "Invalid date"
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = timeZone
            return sdf.format(Date(timestampMillis))
        } catch (e: Exception) {
            // Log error or return a fallback
            return "Error formatting date"
        }
    }
}
