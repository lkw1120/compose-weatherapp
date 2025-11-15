package app.kwlee.weatherapp.feature.bookmarks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.usecase.ObserveFavoriteLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ToggleFavoriteLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookmarksUiState(
    val locations: List<FavoriteLocation> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    observeFavoriteLocationsUseCase: ObserveFavoriteLocationsUseCase,
    private val toggleFavoriteLocationUseCase: ToggleFavoriteLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState

    init {
        viewModelScope.launch {
            observeFavoriteLocationsUseCase()
                .collectLatest { favorites ->
                    _uiState.update {
                        it.copy(
                            locations = favorites,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onBookmarkToggle(location: FavoriteLocation) {
        viewModelScope.launch {
            toggleFavoriteLocationUseCase(location)
                .onFailure { throwable ->
                    AppLogger.e(throwable, "Failed to toggle favorite from bookmarks screen")
                }
        }
    }
}

