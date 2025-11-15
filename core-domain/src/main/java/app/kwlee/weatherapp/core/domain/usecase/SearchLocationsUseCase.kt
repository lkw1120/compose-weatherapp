package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import javax.inject.Inject


/**
 * UseCase for searching locations by query string.
 *
 * Validates and processes the search query before delegating to the repository.
 * Includes business logic:
 * - Empty query validation (returns empty list)
 * - Query trimming (removes leading/trailing whitespace)
 *
 * Future enhancements could include:
 * - Query sanitization
 * - Search result caching
 * - Search history integration
 * - Debouncing logic
 *
 * @param weatherRepository Repository for weather and location operations
 */
class SearchLocationsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 5): Result<List<FavoriteLocation>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return weatherRepository.searchLocations(query.trim(), limit)
    }
}

