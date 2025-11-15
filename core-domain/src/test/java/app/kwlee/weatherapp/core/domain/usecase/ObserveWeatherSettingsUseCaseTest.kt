package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ObserveWeatherSettingsUseCaseTest {

    private lateinit var mockRepository: MockSettingsRepository
    private lateinit var useCase: ObserveWeatherSettingsUseCase

    @Before
    fun setup() {
        mockRepository = MockSettingsRepository()
        useCase = ObserveWeatherSettingsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow of weather settings`() = runTest {
        val settings = WeatherSettings(
            temperatureUnit = TemperatureUnit.FAHRENHEIT,
            windSpeedUnit = WindSpeedUnit.MILES_PER_HOUR,
        )
        mockRepository.setSettings(settings)

        val result = useCase().first()

        assertEquals(settings, result)
    }

    @Test
    fun `invoke returns default settings flow`() = runTest {
        val defaultSettings = WeatherSettings()
        mockRepository.setSettings(defaultSettings)

        val result = useCase().first()

        assertEquals(defaultSettings, result)
    }

    private class MockSettingsRepository : SettingsRepository {
        private var settings: WeatherSettings = WeatherSettings()

        fun setSettings(settings: WeatherSettings) {
            this.settings = settings
        }

        override fun observeSettings() = flowOf(settings)

        override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun restoreDefaults(): Result<WeatherSettings> {
            return Result.success(WeatherSettings())
        }
    }
}

