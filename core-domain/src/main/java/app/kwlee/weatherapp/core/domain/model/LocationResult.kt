package app.kwlee.weatherapp.core.domain.model

sealed class LocationResult {
    data class Success(val location: FavoriteLocation) : LocationResult()
    object PermissionRequired : LocationResult()
    object ProviderDisabled : LocationResult()
    data class Error(val throwable: Throwable) : LocationResult()
}
