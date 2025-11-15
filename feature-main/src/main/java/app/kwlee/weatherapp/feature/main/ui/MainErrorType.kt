package app.kwlee.weatherapp.feature.main.ui

import androidx.annotation.StringRes
import app.kwlee.weatherapp.core.ui.R

/**
 * Error types for Main screen.
 * Platform-independent error representation.
 */
enum class MainErrorType {
    LOCATION_PERMISSION_REQUIRED,
    LOCATION_PROVIDER_DISABLED,
    LOCATION_UNAVAILABLE,
    WEATHER_FETCH_FAILED,
    NETWORK_OFFLINE,
    NETWORK_OFFLINE_NO_CACHE,
    NETWORK_BACK_ONLINE,
}

/**
 * Maps error type to string resource ID.
 * This mapping is done in UI layer to keep ViewModel platform-independent.
 */
@StringRes
fun MainErrorType.toResourceId(): Int {
    return when (this) {
        MainErrorType.LOCATION_PERMISSION_REQUIRED -> R.string.main_error_location_permission
        MainErrorType.LOCATION_PROVIDER_DISABLED -> R.string.main_error_location_provider_disabled
        MainErrorType.LOCATION_UNAVAILABLE -> R.string.main_error_location_unavailable
        MainErrorType.WEATHER_FETCH_FAILED -> R.string.main_error_weather_fetch
        MainErrorType.NETWORK_OFFLINE -> R.string.network_offline
        MainErrorType.NETWORK_OFFLINE_NO_CACHE -> R.string.network_offline_no_cache
        MainErrorType.NETWORK_BACK_ONLINE -> R.string.network_back_online
    }
}

