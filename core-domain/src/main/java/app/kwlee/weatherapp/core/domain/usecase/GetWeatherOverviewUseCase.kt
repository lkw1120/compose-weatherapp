package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import javax.inject.Inject


/**
 * UseCase for fetching weather overview for a given location.
 *
 * This UseCase encapsulates the business logic for retrieving weather data.
 * Currently delegates to the repository, but can be extended with additional
 * business logic such as validation, caching strategies, or data transformation.
 *
 * @param weatherRepository Repository for weather data operations
 */
class GetWeatherOverviewUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(location: FavoriteLocation): Result<WeatherOverview> {
        return weatherRepository.fetchWeatherOverview(location)
    }
}

