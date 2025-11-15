package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RestoreWeatherSettingsUseCaseTest {

    private lateinit var mockRepository: MockSettingsRepository
    private lateinit var useCase: RestoreWeatherSettingsUseCase

    @Before
    fun setup() {
        mockRepository = MockSettingsRepository()
        useCase = RestoreWeatherSettingsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success with default settings`() = runTest {
        val defaultSettings = WeatherSettings()
        mockRepository.setRestoreResult(Result.success(defaultSettings))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(defaultSettings, result.getOrNull())
        assertEquals(1, mockRepository.restoreDefaultsCallCount)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val exception = Exception("DataStore error")
        mockRepository.setRestoreResult(Result.failure(exception))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockSettingsRepository : SettingsRepository {
        private var restoreResult: Result<WeatherSettings> = Result.success(WeatherSettings())
        var restoreDefaultsCallCount = 0

        fun setRestoreResult(result: Result<WeatherSettings>) {
            this.restoreResult = result
        }

        override suspend fun restoreDefaults(): Result<WeatherSettings> {
            restoreDefaultsCallCount++
            return restoreResult
        }

        override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> {
            return Result.success(Unit)
        }

        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(WeatherSettings())
    }
}

