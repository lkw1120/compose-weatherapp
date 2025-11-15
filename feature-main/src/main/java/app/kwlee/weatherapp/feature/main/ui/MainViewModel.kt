package app.kwlee.weatherapp.feature.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.LocationResult
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.usecase.GetCurrentLocationUseCase
import app.kwlee.weatherapp.core.domain.usecase.GetWeatherOverviewUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveFavoriteLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveWeatherSettingsUseCase
import app.kwlee.weatherapp.core.common.NetworkMonitor
import app.kwlee.weatherapp.core.domain.usecase.ToggleFavoriteLocationUseCase
import app.kwlee.weatherapp.core.ui.components.DailyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.HourlyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherHighlightUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherIconType
import app.kwlee.weatherapp.core.ui.mapper.WeatherOverviewUiMapper
import app.kwlee.weatherapp.core.ui.mapper.WeatherAlertUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import app.kwlee.weatherapp.core.domain.model.TimeFormat


data class MainUiState(
    val isLoading: Boolean = true,
    val statusErrorType: MainErrorType? = null,
    val locationLabel: String = "",
    val temperatureLabel: String = "",
    val conditionLabel: String = "",
    val summaryIcon: WeatherIconType = WeatherIconType.Sun,
    val summaryHighlights: List<WeatherHighlightUiModel> = emptyList(),
    val detailHighlights: List<WeatherHighlightUiModel> = emptyList(),
    val hourlyForecast: List<HourlyForecastUiModel> = emptyList(),
    val dailyForecast: List<DailyForecastUiModel> = emptyList(),
    val alerts: List<WeatherAlertUiModel> = emptyList(),
    val isFavorite: Boolean = false,
    val hasWeatherData: Boolean = false,
)


@HiltViewModel
class MainViewModel @Inject constructor(
    private val getWeatherOverviewUseCase: GetWeatherOverviewUseCase,
    private val toggleFavoriteLocationUseCase: ToggleFavoriteLocationUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    observeFavoriteLocationsUseCase: ObserveFavoriteLocationsUseCase,
    private val observeWeatherSettingsUseCase: ObserveWeatherSettingsUseCase,
    private val weatherOverviewUiMapper: WeatherOverviewUiMapper,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private var currentLocation: FavoriteLocation? = null
    private var latestFavorites: List<FavoriteLocation> = emptyList()
    private var hasValidLocation: Boolean = false
    private var currentSettings: WeatherSettings = WeatherSettings()
    private var latestOverview: WeatherOverview? = null

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    init {
        observeFavorites(observeFavoriteLocationsUseCase)
        observeNetworkStatus()

        viewModelScope.launch {
            observeWeatherSettingsUseCase()
                .collectLatest { settings ->
                    val previousSettings = currentSettings
                    currentSettings = settings
                    if (previousSettings == settings) {
                        return@collectLatest
                    }
                    latestOverview?.let { overview ->
                        updateUiWithOverview(
                            overview = overview,
                            isLoading = _uiState.value.isLoading,
                        )
                    }
                }
        }

        refreshCurrentLocation()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            var wasOffline = !networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (!isOnline && !wasOffline) {
                    // Just went offline
                    wasOffline = true
                    if (latestOverview != null) {
                        _uiState.update {
                            it.copy(statusErrorType = MainErrorType.NETWORK_OFFLINE)
                        }
                        _events.emit(MainUiEvent.ShowMessage(MainErrorType.NETWORK_OFFLINE))
                    } else {
                        _uiState.update {
                            it.copy(statusErrorType = MainErrorType.NETWORK_OFFLINE_NO_CACHE)
                        }
                    }
                } else if (isOnline && wasOffline) {
                    // Just came back online
                    wasOffline = false
                    _events.emit(MainUiEvent.ShowMessage(MainErrorType.NETWORK_BACK_ONLINE))
                    // Refresh weather data when back online
                    if (currentLocation != null && hasValidLocation) {
                        refreshWeather(currentLocation!!)
                    }
                }
            }
        }
    }

    private fun handleLocationUnavailable(
        errorType: MainErrorType,
        clearCurrentLocation: Boolean,
        invalidateLocation: Boolean,
    ) {
        if (clearCurrentLocation) {
            currentLocation = null
        }
        if (invalidateLocation) {
            hasValidLocation = false
        }
        if (latestOverview != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    statusErrorType = errorType,
                    hasWeatherData = true,
                )
            }
            return
        }

        latestOverview = null
        _uiState.update {
            it.copy(
                isLoading = false,
                statusErrorType = errorType,
                temperatureLabel = "",
                conditionLabel = "",
                locationLabel = "",
                summaryIcon = WeatherIconType.Sun,
                summaryHighlights = emptyList(),
                detailHighlights = emptyList(),
                hourlyForecast = emptyList(),
                dailyForecast = emptyList(),
                isFavorite = false,
                hasWeatherData = false,
            )
        }
    }

    fun refreshCurrentLocation() {
        viewModelScope.launch {
            when (val locationResult = getCurrentLocationUseCase()) {
                is LocationResult.Success -> {
                    hasValidLocation = true
                    currentLocation = locationResult.location
                    refreshWeather(locationResult.location)
                }
                LocationResult.PermissionRequired -> {
                    hasValidLocation = false
                    handleLocationUnavailable(
                        errorType = MainErrorType.LOCATION_PERMISSION_REQUIRED,
                        clearCurrentLocation = true,
                        invalidateLocation = true,
                    )
                    _events.emit(MainUiEvent.ShowMessage(MainErrorType.LOCATION_PERMISSION_REQUIRED))
                }
                LocationResult.ProviderDisabled -> {
                    hasValidLocation = false
                    handleLocationUnavailable(
                        errorType = MainErrorType.LOCATION_PROVIDER_DISABLED,
                        clearCurrentLocation = false,
                        invalidateLocation = true,
                    )
                    _events.emit(MainUiEvent.ShowMessage(MainErrorType.LOCATION_PROVIDER_DISABLED))
                }
                is LocationResult.Error -> {
                    hasValidLocation = false
                    AppLogger.e(locationResult.throwable, "Failed to obtain current location")
                    handleLocationUnavailable(
                        errorType = MainErrorType.LOCATION_UNAVAILABLE,
                        clearCurrentLocation = true,
                        invalidateLocation = true,
                    )
                }
            }
        }
    }

    fun refreshCurrentWeather() {
        val targetLocation = currentLocation
        if (targetLocation != null && hasValidLocation) {
            refreshWeather(targetLocation)
        } else {
            refreshCurrentLocation()
        }
    }

    fun onLocationPermissionGranted() {
        refreshCurrentLocation()
    }

    fun onLocationPermissionDenied() {
        hasValidLocation = false
        handleLocationUnavailable(
            errorType = MainErrorType.LOCATION_PERMISSION_REQUIRED,
            clearCurrentLocation = true,
            invalidateLocation = true,
        )
    }

    fun refreshWeather(location: FavoriteLocation) {
        currentLocation = location
        hasValidLocation = true
        updateFavoriteState()
        viewModelScope.launch {
            val refreshStart = Instant.now()
            _uiState.update { it.copy(isLoading = true, statusErrorType = null) }
            val result = getWeatherOverviewUseCase(location)
            result.onSuccess { overview ->
                currentLocation = (currentLocation ?: location).copy(name = overview.locationName)
                updateUiWithOverview(
                    overview = overview,
                    isLoading = true,
                )
                ensureMinimumLoadingDelay(refreshStart)
                _uiState.update { it.copy(isLoading = false, statusErrorType = null, hasWeatherData = true) }
            }.onFailure { throwable ->
                AppLogger.e(throwable, "Failed to fetch weather overview")
                ensureMinimumLoadingDelay(refreshStart)
                val isOffline = !networkMonitor.isCurrentlyOnline()
                if (latestOverview != null) {
                    val errorType = if (isOffline) {
                        MainErrorType.NETWORK_OFFLINE
                    } else {
                        MainErrorType.WEATHER_FETCH_FAILED
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusErrorType = errorType,
                            hasWeatherData = true,
                        )
                    }
                    if (!isOffline) {
                        viewModelScope.launch {
                            _events.emit(MainUiEvent.ShowMessage(MainErrorType.WEATHER_FETCH_FAILED))
                        }
                    }
                } else {
                    val errorType = if (isOffline) {
                        MainErrorType.NETWORK_OFFLINE_NO_CACHE
                    } else {
                        MainErrorType.WEATHER_FETCH_FAILED
                    }
                    handleLocationUnavailable(
                        errorType = errorType,
                        clearCurrentLocation = false,
                        invalidateLocation = false,
                    )
                }
            }
        }
    }

    fun onFavoriteClicked() {
        val location = currentLocation ?: return
        viewModelScope.launch {
            toggleFavoriteLocationUseCase(location)
                .onSuccess { isFavorite ->
                    _uiState.update { it.copy(isFavorite = isFavorite) }
                }
                .onFailure { AppLogger.e(it, "Failed to toggle favorite location") }
        }
    }

    private fun observeFavorites(
        observeFavoriteLocationsUseCase: ObserveFavoriteLocationsUseCase,
    ) {
        viewModelScope.launch {
            observeFavoriteLocationsUseCase()
                .collectLatest { favorites ->
                    latestFavorites = favorites
                    updateFavoriteState()
                }
        }
    }

    private fun FavoriteLocation.isSameLocation(other: FavoriteLocation?): Boolean {
        return other != null &&
            latitude == other.latitude &&
            longitude == other.longitude
    }

    private fun updateFavoriteState() {
        val target = currentLocation
        val isFavorite = target != null && latestFavorites.any { favorite -> favorite.isSameLocation(target) }
        _uiState.update { it.copy(isFavorite = isFavorite) }
    }

    private fun updateUiWithOverview(
        overview: WeatherOverview,
        isLoading: Boolean,
    ) {
        latestOverview = overview
        val display = weatherOverviewUiMapper.map(overview, currentSettings)
        AppLogger.d(
            "Weather overview updated: hourly=${display.hourlyForecast.size}, daily=${display.dailyForecast.size}, alerts=${display.alerts.size}",
        )
        AppLogger.d("Overview alerts count: ${overview.alerts.size}")
        _uiState.update { state ->
            state.copy(
                isLoading = isLoading,
                statusErrorType = null,
                locationLabel = display.locationLabel,
                temperatureLabel = display.temperatureLabel,
                conditionLabel = display.conditionLabel,
                summaryIcon = display.summaryIcon,
                summaryHighlights = display.summaryHighlights,
                detailHighlights = display.detailHighlights,
                hourlyForecast = display.hourlyForecast,
                dailyForecast = display.dailyForecast,
                alerts = display.alerts,
                isFavorite = state.isFavorite,
                hasWeatherData = true,
            )
        }
    }

    companion object {
        private val MIN_REFRESH_VISIBLE_DURATION: Duration = Duration.ofSeconds(1)
    }

    private suspend fun ensureMinimumLoadingDelay(startInstant: Instant) {
        val elapsed = Duration.between(startInstant, Instant.now())
        if (elapsed >= MIN_REFRESH_VISIBLE_DURATION) {
            return
        }
        delay(MIN_REFRESH_VISIBLE_DURATION.minus(elapsed).toMillis())
    }
}


sealed interface MainUiEvent {
    data class ShowMessage(
        val errorType: MainErrorType,
        val formatArgs: List<Any> = emptyList(),
    ) : MainUiEvent
}