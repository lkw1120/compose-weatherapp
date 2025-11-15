package app.kwlee.weatherapp.feature.weatherpreview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.usecase.GetWeatherOverviewUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveWeatherSettingsUseCase
import app.kwlee.weatherapp.core.ui.mapper.WeatherOverviewUiModel
import app.kwlee.weatherapp.core.ui.mapper.WeatherOverviewUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherPreviewViewModel @Inject constructor(
    private val getWeatherOverviewUseCase: GetWeatherOverviewUseCase,
    observeWeatherSettingsUseCase: ObserveWeatherSettingsUseCase,
    private val weatherOverviewUiMapper: WeatherOverviewUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherPreviewUiState())
    val uiState: StateFlow<WeatherPreviewUiState> = _uiState.asStateFlow()

    private var currentSettings: WeatherSettings = WeatherSettings()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            observeWeatherSettingsUseCase()
                .collect { settings ->
                    currentSettings = settings
                    _uiState.update { state ->
                        val overview = state.overview ?: return@update state
                        state.copy(weather = weatherOverviewUiMapper.map(overview, currentSettings))
                    }
                }
        }
    }

    fun present(location: FavoriteLocation) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    location = location,
                    overview = null,
                    weather = null,
                    isLoading = true,
                    errorMessage = null,
                )
            }

            getWeatherOverviewUseCase(location)
                .onSuccess { overview ->
                    _uiState.update {
                        it.copy(
                            overview = overview,
                            weather = weatherOverviewUiMapper.map(overview, currentSettings),
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to load weather preview")
                    _uiState.update {
                        it.copy(
                            overview = null,
                            weather = null,
                            isLoading = false,
                            errorMessage = throwable.message
                        )
                    }
                }
        }
    }

    fun dismiss() {
        loadJob?.cancel()
        _uiState.value = WeatherPreviewUiState()
    }
}

data class WeatherPreviewUiState(
    val location: FavoriteLocation? = null,
    val overview: WeatherOverview? = null,
    val weather: WeatherOverviewUiModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val shouldShowSheet: Boolean
        get() = isLoading || weather != null || errorMessage != null
}

