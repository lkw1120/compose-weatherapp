package app.kwlee.weatherapp.feature.main.ui

sealed interface MainPermissionEvent {
    data object Granted : MainPermissionEvent
    data object Denied : MainPermissionEvent
}
