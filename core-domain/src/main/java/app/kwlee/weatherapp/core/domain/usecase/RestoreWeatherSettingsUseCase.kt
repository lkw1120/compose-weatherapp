package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import javax.inject.Inject


/**
 * UseCase for restoring weather settings to default values.
 *
 * Resets all weather settings to their default values and returns the restored settings.
 * Future enhancements could include:
 * - Confirmation logic
 * - Analytics tracking
 * - Partial restore options
 *
 * @param settingsRepository Repository for weather settings operations
 */
class RestoreWeatherSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<WeatherSettings> {
        return settingsRepository.restoreDefaults()
    }
}

