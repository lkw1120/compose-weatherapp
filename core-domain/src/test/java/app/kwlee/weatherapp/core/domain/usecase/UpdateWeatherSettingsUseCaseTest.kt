package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class UpdateWeatherSettingsUseCaseTest {

    private lateinit var mockRepository: MockSettingsRepository
    private lateinit var useCase: UpdateWeatherSettingsUseCase

    @Before
    fun setup() {
        mockRepository = MockSettingsRepository()
        useCase = UpdateWeatherSettingsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val settings = WeatherSettings(
            temperatureUnit = TemperatureUnit.FAHRENHEIT,
            windSpeedUnit = WindSpeedUnit.MILES_PER_HOUR,
        )
        mockRepository.setUpdateResult(Result.success(Unit))

        val result = useCase(settings)

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        assertEquals(1, mockRepository.updateSettingsCallCount)
        assertEquals(settings, mockRepository.lastSettings)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val settings = WeatherSettings()
        val exception = Exception("DataStore error")
        mockRepository.setUpdateResult(Result.failure(exception))

        val result = useCase(settings)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockSettingsRepository : SettingsRepository {
        private var updateResult: Result<Unit> = Result.success(Unit)
        var updateSettingsCallCount = 0
        var lastSettings: WeatherSettings? = null

        fun setUpdateResult(result: Result<Unit>) {
            this.updateResult = result
        }

        override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> {
            updateSettingsCallCount++
            lastSettings = settings
            return updateResult
        }

        override fun observeSettings() = kotlinx.coroutines.flow.flowOf(WeatherSettings())

        override suspend fun restoreDefaults(): Result<WeatherSettings> {
            return Result.success(WeatherSettings())
        }
    }
}

