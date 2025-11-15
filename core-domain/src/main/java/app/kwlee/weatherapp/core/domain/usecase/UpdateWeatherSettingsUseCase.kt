package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import javax.inject.Inject


/**
 * UseCase for updating weather settings.
 *
 * Encapsulates the business logic for persisting weather settings changes.
 * Future enhancements could include:
 * - Settings validation
 * - Change tracking/analytics
 * - Settings migration logic
 *
 * @param settingsRepository Repository for weather settings operations
 */
class UpdateWeatherSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(settings: WeatherSettings): Result<Unit> {
        return settingsRepository.updateSettings(settings)
    }
}

