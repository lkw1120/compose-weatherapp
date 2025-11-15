package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import javax.inject.Inject


/**
 * UseCase for clearing weather cache.
 *
 * Clears all cached weather data from the local database.
 */
class ClearWeatherCacheUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return weatherRepository.clearCache()
    }
}

