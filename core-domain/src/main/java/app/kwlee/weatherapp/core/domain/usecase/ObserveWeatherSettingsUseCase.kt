package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow


/**
 * UseCase for observing weather settings changes.
 *
 * Provides a reactive stream of weather settings that updates automatically
 * when settings are modified. This abstraction maintains separation between
 * the UI layer and data layer, enabling easier testing and future enhancements.
 *
 * @param settingsRepository Repository for weather settings operations
 */
class ObserveWeatherSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<WeatherSettings> = settingsRepository.observeSettings()
}

