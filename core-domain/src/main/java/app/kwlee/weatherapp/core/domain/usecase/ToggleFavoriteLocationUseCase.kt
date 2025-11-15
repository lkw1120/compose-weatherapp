package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import javax.inject.Inject


/**
 * UseCase for toggling favorite status of a location.
 *
 * Adds a location to favorites if not present, or removes it if already favorited.
 * Returns true if the location was added, false if removed.
 *
 * Future enhancements could include:
 * - Validation logic (e.g., maximum number of favorites)
 * - Analytics tracking
 * - Synchronization with cloud services
 *
 * @param favoriteRepository Repository for favorite location operations
 */
class ToggleFavoriteLocationUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(location: FavoriteLocation): Result<Boolean> {
        return favoriteRepository.toggleFavorite(location)
    }
}

