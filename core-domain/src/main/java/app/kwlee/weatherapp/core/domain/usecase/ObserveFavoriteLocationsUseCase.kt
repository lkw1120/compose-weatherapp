package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * UseCase for observing favorite locations.
 *
 * Provides a reactive stream of favorite locations that updates automatically
 * when favorites change. This abstraction allows for future enhancements such as
 * filtering, sorting, or additional business logic.
 *
 * @param favoriteRepository Repository for favorite location operations
 */
class ObserveFavoriteLocationsUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(): Flow<List<FavoriteLocation>> = favoriteRepository.observeFavorites()
}

