package app.kwlee.weatherapp.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.domain.model.DistanceUnit
import app.kwlee.weatherapp.core.domain.model.PrecipitationUnit
import app.kwlee.weatherapp.core.domain.model.PressureUnit
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.TimeFormat
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.domain.usecase.ClearWeatherCacheUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveWeatherSettingsUseCase
import app.kwlee.weatherapp.core.domain.usecase.RestoreWeatherSettingsUseCase
import app.kwlee.weatherapp.core.domain.usecase.UpdateWeatherSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeWeatherSettingsUseCase: ObserveWeatherSettingsUseCase,
    private val updateWeatherSettingsUseCase: UpdateWeatherSettingsUseCase,
    private val restoreWeatherSettingsUseCase: RestoreWeatherSettingsUseCase,
    private val clearWeatherCacheUseCase: ClearWeatherCacheUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsUiEvent>()
    val events: SharedFlow<SettingsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeWeatherSettingsUseCase()
                .catch { throwable ->
                    AppLogger.e(throwable, "Failed to observe settings")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaving = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
                .collect { settings ->
                    _uiState.update {
                        it.copy(
                            settings = settings,
                            isLoading = false,
                            isSaving = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun onTemperatureUnitSelected(unit: TemperatureUnit) {
        updateSettings { it.copy(temperatureUnit = unit) }
    }

    fun onWindSpeedUnitSelected(unit: WindSpeedUnit) {
        updateSettings { it.copy(windSpeedUnit = unit) }
    }

    fun onPrecipitationUnitSelected(unit: PrecipitationUnit) {
        updateSettings { it.copy(precipitationUnit = unit) }
    }

    fun onDistanceUnitSelected(unit: DistanceUnit) {
        updateSettings { it.copy(distanceUnit = unit) }
    }

    fun onPressureUnitSelected(unit: PressureUnit) {
        updateSettings { it.copy(pressureUnit = unit) }
    }

    fun onTimeFormatSelected(format: TimeFormat) {
        updateSettings { it.copy(timeFormat = format) }
    }

    fun onRestoreDefaults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            restoreWeatherSettingsUseCase()
                .onSuccess { defaults ->
                    _uiState.update {
                        it.copy(
                            settings = defaults,
                            isSaving = false,
                            errorMessage = null,
                        )
                    }
                    _events.emit(SettingsUiEvent.ShowSnackbar(SettingsSnackbarMessage.RestoreDefaultsSuccess))
                }
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to restore settings defaults")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
        }
    }

    fun onClearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            clearWeatherCacheUseCase()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = null,
                        )
                    }
                    _events.emit(SettingsUiEvent.ShowSnackbar(SettingsSnackbarMessage.ClearCacheSuccess))
                }
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to clear cache")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
        }
    }

    private fun updateSettings(transform: (WeatherSettings) -> WeatherSettings) {
        val currentSettings = _uiState.value.settings
        val updatedSettings = transform(currentSettings)
        if (updatedSettings == currentSettings) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            updateWeatherSettingsUseCase(updatedSettings)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to update settings")
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            errorMessage = throwable.message,
                        )
                    }
                }
        }
    }
}


data class SettingsUiState(
    val settings: WeatherSettings = WeatherSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SettingsUiEvent {
    data class ShowSnackbar(val message: SettingsSnackbarMessage) : SettingsUiEvent
}

enum class SettingsSnackbarMessage {
    RestoreDefaultsSuccess,
    ClearCacheSuccess,
}

