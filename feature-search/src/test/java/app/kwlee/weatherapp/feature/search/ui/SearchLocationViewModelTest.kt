package app.kwlee.weatherapp.feature.search.ui

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import app.kwlee.weatherapp.core.domain.usecase.AddSearchHistoryEntryUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveFavoriteLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveSearchHistoryUseCase
import app.kwlee.weatherapp.core.domain.usecase.RemoveSearchHistoryEntryUseCase
import app.kwlee.weatherapp.core.domain.usecase.SearchLocationsUseCase
import app.kwlee.weatherapp.core.domain.usecase.ToggleFavoriteLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class SearchLocationViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockWeatherRepository: MockWeatherRepository
    private lateinit var mockFavoriteRepository: MockFavoriteRepository
    private lateinit var mockSearchHistoryRepository: MockSearchHistoryRepository
    private lateinit var searchUseCase: SearchLocationsUseCase
    private lateinit var toggleUseCase: ToggleFavoriteLocationUseCase
    private lateinit var observeFavoritesUseCase: ObserveFavoriteLocationsUseCase
    private lateinit var observeHistoryUseCase: ObserveSearchHistoryUseCase
    private lateinit var addHistoryUseCase: AddSearchHistoryEntryUseCase
    private lateinit var removeHistoryUseCase: RemoveSearchHistoryEntryUseCase
    private lateinit var viewModel: SearchLocationViewModel

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWeatherRepository = MockWeatherRepository()
        mockFavoriteRepository = MockFavoriteRepository()
        mockSearchHistoryRepository = MockSearchHistoryRepository()
        searchUseCase = SearchLocationsUseCase(mockWeatherRepository)
        toggleUseCase = ToggleFavoriteLocationUseCase(mockFavoriteRepository)
        observeFavoritesUseCase = ObserveFavoriteLocationsUseCase(mockFavoriteRepository)
        observeHistoryUseCase = ObserveSearchHistoryUseCase(mockSearchHistoryRepository)
        addHistoryUseCase = AddSearchHistoryEntryUseCase(mockSearchHistoryRepository)
        removeHistoryUseCase = RemoveSearchHistoryEntryUseCase(mockSearchHistoryRepository)
        viewModel = SearchLocationViewModel(
            searchUseCase,
            toggleUseCase,
            observeFavoritesUseCase,
            observeHistoryUseCase,
            addHistoryUseCase,
            removeHistoryUseCase,
        )
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `initial state has empty query and results`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val initialState = viewModel.uiState.value

        assertEquals("", initialState.query)
        assertEquals(emptyList<FavoriteLocation>(), initialState.results)
        assertFalse(initialState.isLoading)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onQueryChange updates query`() = runTest(testDispatcher) {
        viewModel.onQueryChange("Seoul")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Seoul", state.query)
        assertNull(state.errorMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onSearch with blank query clears results`() = runTest(testDispatcher) {
        viewModel.onQueryChange("   ")
        viewModel.onSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(emptyList<FavoriteLocation>(), state.results)
        assertFalse(state.isLoading)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onSearch with valid query searches locations`() = runTest(testDispatcher) {
        val locations = listOf(createTestLocation("Seoul"))
        mockWeatherRepository.setSearchResult(Result.success(locations))
        mockSearchHistoryRepository.setAddResult(Result.success(Unit))

        viewModel.onQueryChange("Seoul")
        viewModel.onSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(locations, state.results)
        assertFalse(state.isLoading)
        assertEquals(1, mockWeatherRepository.searchLocationsCallCount)
        assertEquals(1, mockSearchHistoryRepository.addQueryCallCount)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onSearch sets error message on failure`() = runTest(testDispatcher) {
        val exception = Exception("Search failed")
        mockWeatherRepository.setSearchResult(Result.failure(exception))

        viewModel.onQueryChange("Seoul")
        viewModel.onSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Search failed", state.errorMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `clearQuery clears query and results`() = runTest(testDispatcher) {
        viewModel.onQueryChange("Seoul")
        viewModel.clearQuery()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(emptyList<FavoriteLocation>(), state.results)
        assertFalse(state.isLoading)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onHistorySelect sets query and searches`() = runTest(testDispatcher) {
        val locations = listOf(createTestLocation("Seoul"))
        mockWeatherRepository.setSearchResult(Result.success(locations))
        mockSearchHistoryRepository.setAddResult(Result.success(Unit))

        viewModel.onHistorySelect("Seoul")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Seoul", state.query)
        assertEquals(locations, state.results)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onHistoryDelete removes history entry`() = runTest(testDispatcher) {
        mockSearchHistoryRepository.setRemoveResult(Result.success(Unit))

        viewModel.onHistoryDelete("Seoul")
        advanceUntilIdle()

        assertEquals(1, mockSearchHistoryRepository.removeQueryCallCount)
        assertEquals("Seoul", mockSearchHistoryRepository.lastQuery)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onBookmarkToggle calls toggle use case`() = runTest(testDispatcher) {
        val location = createTestLocation("Seoul")
        mockFavoriteRepository.setToggleResult(Result.success(true))

        viewModel.onBookmarkToggle(location)
        advanceUntilIdle()

        assertEquals(1, mockFavoriteRepository.toggleFavoriteCallCount)
        assertEquals(location, mockFavoriteRepository.lastLocation)
    }

    private fun createTestLocation(name: String): FavoriteLocation {
        return FavoriteLocation(
            name = name,
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private class MockWeatherRepository : WeatherRepository {
        private var searchResult: Result<List<FavoriteLocation>> = Result.success(emptyList())
        var searchLocationsCallCount = 0

        fun setSearchResult(result: Result<List<FavoriteLocation>>) {
            this.searchResult = result
        }

        override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> {
            searchLocationsCallCount++
            return searchResult
        }

        override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<app.kwlee.weatherapp.core.domain.model.WeatherOverview> {
            return Result.failure(Exception("Not implemented"))
        }

        override suspend fun clearCache(): Result<Unit> {
            return Result.success(Unit)
        }
    }

    private class MockFavoriteRepository : FavoriteRepository {
        private var toggleResult: Result<Boolean> = Result.success(true)
        var toggleFavoriteCallCount = 0
        var lastLocation: FavoriteLocation? = null

        fun setToggleResult(result: Result<Boolean>) {
            this.toggleResult = result
        }

        override fun observeFavorites() = flowOf(emptyList<FavoriteLocation>())

        override suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean> {
            toggleFavoriteCallCount++
            lastLocation = location
            return toggleResult
        }

        override suspend fun isFavorite(location: FavoriteLocation): Result<Boolean> {
            return Result.success(false)
        }
    }

    private class MockSearchHistoryRepository : SearchHistoryRepository {
        private var addResult: Result<Unit> = Result.success(Unit)
        private var removeResult: Result<Unit> = Result.success(Unit)
        var addQueryCallCount = 0
        var removeQueryCallCount = 0
        var lastQuery: String? = null

        fun setAddResult(result: Result<Unit>) {
            this.addResult = result
        }

        fun setRemoveResult(result: Result<Unit>) {
            this.removeResult = result
        }

        override fun observeHistory() = flowOf(emptyList<String>())

        override suspend fun addQuery(query: String): Result<Unit> {
            addQueryCallCount++
            return addResult
        }

        override suspend fun removeQuery(query: String): Result<Unit> {
            removeQueryCallCount++
            lastQuery = query
            return removeResult
        }
    }
}

