package app.kwlee.weatherapp.feature.bookmarks.ui

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import app.kwlee.weatherapp.core.domain.usecase.ObserveFavoriteLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ToggleFavoriteLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class BookmarksViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: MockFavoriteRepository
    private lateinit var observeUseCase: ObserveFavoriteLocationsUseCase
    private lateinit var toggleUseCase: ToggleFavoriteLocationUseCase
    private lateinit var viewModel: BookmarksViewModel

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockFavoriteRepository()
        observeUseCase = ObserveFavoriteLocationsUseCase(mockRepository)
        toggleUseCase = ToggleFavoriteLocationUseCase(mockRepository)
        viewModel = BookmarksViewModel(observeUseCase, toggleUseCase)
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `initial state has empty locations and isLoading false after flow emits`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val initialState = viewModel.uiState.value

        assertFalse("isLoading should be false after Flow emits initial empty list", initialState.isLoading)
        assertEquals(emptyList<FavoriteLocation>(), initialState.locations)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `uiState updates when favorites are observed`() = runTest(testDispatcher) {
        val locations = listOf(
            createTestLocation("Seoul"),
            createTestLocation("Busan"),
        )
        mockRepository.setFavorites(locations)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(locations, state.locations)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onBookmarkToggle calls toggle use case`() = runTest(testDispatcher) {
        val location = createTestLocation("Seoul")
        mockRepository.setToggleResult(Result.success(true))

        viewModel.onBookmarkToggle(location)
        advanceUntilIdle()

        assertEquals(1, mockRepository.toggleFavoriteCallCount)
        assertEquals(location, mockRepository.lastLocation)
    }

    private fun createTestLocation(name: String): FavoriteLocation {
        return FavoriteLocation(
            name = name,
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private class MockFavoriteRepository : FavoriteRepository {
        private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
        private var toggleResult: Result<Boolean> = Result.success(true)
        var toggleFavoriteCallCount = 0
        var lastLocation: FavoriteLocation? = null

        fun setFavorites(favorites: List<FavoriteLocation>) {
            _favorites.value = favorites
        }

        fun setToggleResult(result: Result<Boolean>) {
            this.toggleResult = result
        }

        override fun observeFavorites(): Flow<List<FavoriteLocation>> = _favorites

        override suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean> {
            toggleFavoriteCallCount++
            lastLocation = location
            return toggleResult
        }

        override suspend fun isFavorite(location: FavoriteLocation): Result<Boolean> {
            return Result.success(false)
        }
    }
}

