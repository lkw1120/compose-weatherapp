package app.kwlee.weatherapp.feature.search.ui

import androidx.annotation.StringRes
import app.kwlee.weatherapp.core.ui.R

/**
 * Error types for Search screen.
 * Platform-independent error representation.
 */
enum class SearchErrorType {
    SEARCH_FAILED,
}

/**
 * Maps error type to string resource ID.
 * This mapping is done in UI layer to keep ViewModel platform-independent.
 */
@StringRes
fun SearchErrorType.toResourceId(): Int {
    return when (this) {
        SearchErrorType.SEARCH_FAILED -> R.string.search_error_failed
    }
}

