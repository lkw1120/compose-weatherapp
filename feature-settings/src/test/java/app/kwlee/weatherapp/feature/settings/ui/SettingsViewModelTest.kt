package app.kwlee.weatherapp.feature.settings.ui

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import app.kwlee.weatherapp.core.domain.usecase.ClearWeatherCacheUseCase
import app.kwlee.weatherapp.core.domain.usecase.ObserveWeatherSettingsUseCase
import app.kwlee.weatherapp.core.domain.usecase.RestoreWeatherSettingsUseCase
import app.kwlee.weatherapp.core.domain.usecase.UpdateWeatherSettingsUseCase
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


class SettingsViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: MockSettingsRepository
    private lateinit var mockWeatherRepository: MockWeatherRepository
    private lateinit var observeUseCase: ObserveWeatherSettingsUseCase
    private lateinit var updateUseCase: UpdateWeatherSettingsUseCase
    private lateinit var restoreUseCase: RestoreWeatherSettingsUseCase
    private lateinit var clearUseCase: ClearWeatherCacheUseCase
    private lateinit var viewModel: SettingsViewModel

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockSettingsRepository()
        mockWeatherRepository = MockWeatherRepository()
        observeUseCase = ObserveWeatherSettingsUseCase(mockRepository)
        updateUseCase = UpdateWeatherSettingsUseCase(mockRepository)
        restoreUseCase = RestoreWeatherSettingsUseCase(mockRepository)
        clearUseCase = ClearWeatherCacheUseCase(mockWeatherRepository)
        viewModel = SettingsViewModel(observeUseCase, updateUseCase, restoreUseCase, clearUseCase)
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `initial state has default settings and isLoading false after flow emits`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val initialState = viewModel.uiState.value

        assertFalse("isLoading should be false after Flow emits initial settings", initialState.isLoading)
        assertEquals(WeatherSettings(), initialState.settings)
        assertFalse(initialState.isSaving)
        assertNull(initialState.errorMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `uiState updates when settings are observed`() = runTest(testDispatcher) {
        val settings = WeatherSettings(
            temperatureUnit = TemperatureUnit.FAHRENHEIT,
            windSpeedUnit = WindSpeedUnit.MILES_PER_HOUR,
        )
        mockRepository.setSettings(settings)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(settings, state.settings)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onTemperatureUnitSelected updates settings`() = runTest(testDispatcher) {
        mockRepository.setSettings(WeatherSettings())
        mockRepository.setUpdateResult(Result.success(Unit))
        advanceUntilIdle()

        viewModel.onTemperatureUnitSelected(TemperatureUnit.FAHRENHEIT)
        advanceUntilIdle()

        assertEquals(1, mockRepository.updateSettingsCallCount)
        val updatedSettings = mockRepository.lastSettings
        assertEquals(TemperatureUnit.FAHRENHEIT, updatedSettings?.temperatureUnit)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `onRestoreDefaults restores default settings`() = runTest(testDispatcher) {
        val defaultSettings = WeatherSettings()
        mockRepository.setRestoreResult(Result.success(defaultSettings))

        viewModel.onRestoreDefaults()
        advanceUntilIdle()

        assertEquals(1, mockRepository.restoreDefaultsCallCount)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `updateSettings sets isSaving to true then false on success`() = runTest(testDispatcher) {
        mockRepository.setSettings(WeatherSettings())
        mockRepository.setUpdateResult(Result.success(Unit))
        advanceUntilIdle()

        viewModel.onTemperatureUnitSelected(TemperatureUnit.FAHRENHEIT)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isSaving)
        assertNull(finalState.errorMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `updateSettings sets error message on failure`() = runTest(testDispatcher) {
        mockRepository.setSettings(WeatherSettings())
        val exception = Exception("Update failed")
        mockRepository.setUpdateResult(Result.failure(exception))
        advanceUntilIdle()

        viewModel.onTemperatureUnitSelected(TemperatureUnit.FAHRENHEIT)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isSaving)
        assertEquals("Update failed", finalState.errorMessage)
    }

    private class MockSettingsRepository : SettingsRepository {
        private val _settings = MutableStateFlow(WeatherSettings())
        private var updateResult: Result<Unit> = Result.success(Unit)
        private var restoreResult: Result<WeatherSettings> = Result.success(WeatherSettings())
        var updateSettingsCallCount = 0
        var restoreDefaultsCallCount = 0
        var lastSettings: WeatherSettings? = null

        fun setSettings(settings: WeatherSettings) {
            _settings.value = settings
        }

        fun setUpdateResult(result: Result<Unit>) {
            this.updateResult = result
        }

        fun setRestoreResult(result: Result<WeatherSettings>) {
            this.restoreResult = result
        }

        override fun observeSettings() = _settings

        override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> {
            updateSettingsCallCount++
            lastSettings = settings
            return updateResult
        }

        override suspend fun restoreDefaults(): Result<WeatherSettings> {
            restoreDefaultsCallCount++
            return restoreResult
        }
    }

    private class MockWeatherRepository : WeatherRepository {
        override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> {
            return Result.success(emptyList())
        }

        override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<app.kwlee.weatherapp.core.domain.model.WeatherOverview> {
            return Result.failure(Exception("Not implemented"))
        }

        override suspend fun clearCache(): Result<Unit> {
            return Result.success(Unit)
        }
    }
}

