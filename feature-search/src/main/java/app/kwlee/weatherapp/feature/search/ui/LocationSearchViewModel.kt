package app.kwlee.weatherapp.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.usecase.ObserveFavoriteLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveSearchHistoryUseCase
import app.kwlee.weatherapp.core.domain.usecase.SearchLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ToggleFavoriteLocationUseCase
import app.kwlee.weatherapp.core.domain.usecase.AddSearchHistoryEntryUseCase
import app.kwlee.weatherapp.core.domain.usecase.RemoveSearchHistoryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class SearchLocationViewModel @Inject constructor(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val toggleFavoriteLocationUseCase: ToggleFavoriteLocationUseCase,
    observeFavoriteLocationsUseCase: ObserveFavoriteLocationsUseCase,
    observeSearchHistoryUseCase: ObserveSearchHistoryUseCase,
    private val addSearchHistoryEntryUseCase: AddSearchHistoryEntryUseCase,
    private val removeSearchHistoryEntryUseCase: RemoveSearchHistoryEntryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSearchUiState())
    val uiState: StateFlow<LocationSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            observeFavoriteLocationsUseCase()
                .collect { favorites ->
                    _uiState.update {
                        it.copy(
                            favoriteKeys = favorites.map { favorite -> favorite.toLocationKey() }.toSet(),
                        )
                    }
                }
        }

        viewModelScope.launch {
            observeSearchHistoryUseCase()
                .collect { history ->
                    _uiState.update { it.copy(history = history) }
                }
        }
    }

    fun onBookmarkToggle(location: FavoriteLocation) {
        viewModelScope.launch {
            toggleFavoriteLocationUseCase(location)
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to toggle favorite from search")
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, errorMessage = null, errorType = null) }
    }

    fun onSearch() {
        val currentQuery = _uiState.value.query.trim()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (currentQuery.isBlank()) {
                _uiState.update {
                    it.copy(
                        results = emptyList(),
                        errorMessage = null,
                        isLoading = false,
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            searchLocationsUseCase(currentQuery)
                .onSuccess { locations ->
                    _uiState.update {
                        it.copy(isLoading = false, results = locations, errorMessage = null, errorType = null)
                    }
                    addSearchHistoryEntryUseCase(currentQuery)
                        .onFailure { throwable ->
                            AppLogger.e(throwable, "Failed to store search history entry")
                        }
                }
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to search locations")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = emptyList(),
                            errorType = if (throwable.message.isNullOrBlank()) {
                                SearchErrorType.SEARCH_FAILED
                            } else {
                                null
                            },
                            errorMessage = throwable.message,
                        )
                    }
                }
        }
    }

    fun clearQuery() {
        searchJob?.cancel()
            _uiState.update {
                it.copy(
                    query = "",
                    results = emptyList(),
                    errorMessage = null,
                    errorType = null,
                    isLoading = false,
                )
            }
    }

    fun onHistorySelect(query: String) {
        if (query.isBlank()) {
            return
        }
        _uiState.update { it.copy(query = query, errorMessage = null, errorType = null) }
        onSearch()
    }

    fun onHistoryDelete(query: String) {
        viewModelScope.launch {
            removeSearchHistoryEntryUseCase(query)
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to delete search history entry")
                }
        }
    }

    private fun FavoriteLocation.toLocationKey(): LocationKey = LocationKey(latitude, longitude)
}


data class LocationSearchUiState(
    val query: String = "",
    val results: List<FavoriteLocation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorType: SearchErrorType? = null,
    val favoriteKeys: Set<LocationKey> = emptySet(),
    val history: List<String> = emptyList(),
)

data class LocationKey(val latitude: Double, val longitude: Double)

