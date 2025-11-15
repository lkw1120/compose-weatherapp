package app.kwlee.weatherapp.feature.weatherpreview.ui

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import app.kwlee.weatherapp.core.domain.usecase.GetWeatherOverviewUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveWeatherSettingsUseCase
import app.kwlee.weatherapp.core.ui.mapper.WeatherOverviewUiModel
import app.kwlee.weatherapp.core.ui.mapper.WeatherOverviewUiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class WeatherPreviewViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockWeatherRepository: MockWeatherRepository
    private lateinit var mockSettingsRepository: MockSettingsRepository
    private lateinit var getWeatherUseCase: GetWeatherOverviewUseCase
    private lateinit var observeSettingsUseCase: ObserveWeatherSettingsUseCase
    private lateinit var uiMapper: WeatherOverviewUiMapper
    private lateinit var viewModel: WeatherPreviewViewModel

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWeatherRepository = MockWeatherRepository()
        mockSettingsRepository = MockSettingsRepository()
        getWeatherUseCase = GetWeatherOverviewUseCase(mockWeatherRepository)
        observeSettingsUseCase = ObserveWeatherSettingsUseCase(mockSettingsRepository)
        uiMapper = WeatherOverviewUiMapper()
        viewModel = WeatherPreviewViewModel(
            getWeatherUseCase,
            observeSettingsUseCase,
            uiMapper,
        )
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `initial state has no location or weather`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val initialState = viewModel.uiState.value

        assertNull(initialState.location)
        assertNull(initialState.overview)
        assertNull(initialState.weather)
        assertFalse(initialState.isLoading)
        assertNull(initialState.errorMessage)
        assertFalse(initialState.shouldShowSheet)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present sets isLoading to true`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val overview = createTestWeatherOverview()
        // fetchWeatherOverview가 지연되도록 설정하여 isLoading이 true인 상태를 확인할 수 있도록 함
        mockWeatherRepository.setFetchResult(Result.success(overview))
        mockWeatherRepository.setDelay(1000L) // 1초 지연

        viewModel.present(location)
        // UnconfinedTestDispatcher는 코루틴을 즉시 실행하지만,
        // fetchWeatherOverview에 지연이 있으므로 isLoading이 true인 상태를 확인할 수 있음
        
        val state = viewModel.uiState.value
        assertEquals(location, state.location)
        assertTrue("isLoading should be true immediately after present()", state.isLoading)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present updates state with weather on success`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val overview = createTestWeatherOverview()
        mockWeatherRepository.setFetchResult(Result.success(overview))

        viewModel.present(location)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(overview, state.overview)
        assertNotNull(state.weather)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.shouldShowSheet)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present sets error message on failure`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val exception = Exception("Network error")
        mockWeatherRepository.setFetchResult(Result.failure(exception))

        viewModel.present(location)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.shouldShowSheet)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `dismiss resets state`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val overview = createTestWeatherOverview()
        mockWeatherRepository.setFetchResult(Result.success(overview))

        viewModel.present(location)
        advanceUntilIdle()
        viewModel.dismiss()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.location)
        assertNull(state.overview)
        assertNull(state.weather)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    private fun createTestLocation(): FavoriteLocation {
        return FavoriteLocation(
            name = "Seoul",
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private fun createTestWeatherOverview(): WeatherOverview {
        return WeatherOverview(
            locationName = "Seoul",
            temperatureCelsius = 20.0,
            feelsLikeCelsius = 19.0,
            conditionDescription = "Clear sky",
            conditionType = WeatherConditionType.CLEAR,
            humidityPercent = 50,
            precipitationMm = 0.0,
            highlights = emptyList(),
            hourly = emptyList(),
            daily = emptyList(),
        )
    }

    private class MockWeatherRepository : WeatherRepository {
        private var fetchResult: Result<WeatherOverview> = Result.success(createTestWeatherOverview())
        private var delayMs: Long = 0L

        fun setFetchResult(result: Result<WeatherOverview>) {
            this.fetchResult = result
        }

        fun setDelay(delayMs: Long) {
            this.delayMs = delayMs
        }

        override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<WeatherOverview> {
            if (delayMs > 0) {
                delay(delayMs)
            }
            return fetchResult
        }

        override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> {
            return Result.success(emptyList())
        }

        override suspend fun clearCache(): Result<Unit> {
            return Result.success(Unit)
        }

        companion object {
            private fun createTestWeatherOverview(): WeatherOverview {
                return WeatherOverview(
                    locationName = "Seoul",
                    temperatureCelsius = 20.0,
                    feelsLikeCelsius = 19.0,
                    conditionDescription = "Clear sky",
                    conditionType = WeatherConditionType.CLEAR,
                    humidityPercent = 50,
                    precipitationMm = 0.0,
                    highlights = emptyList(),
                    hourly = emptyList(),
                    daily = emptyList(),
                )
            }
        }
    }

    private class MockSettingsRepository : SettingsRepository {
        override fun observeSettings() = flowOf(WeatherSettings())

        override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun restoreDefaults(): Result<WeatherSettings> {
            return Result.success(WeatherSettings())
        }
    }
}

