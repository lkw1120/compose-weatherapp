package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import javax.inject.Inject


/**
 * UseCase for observing search history.
 *
 * Provides a reactive stream of search history entries that updates automatically
 * when history changes. This abstraction maintains separation between UI and data layers.
 *
 * Future enhancements could include:
 * - History filtering/sorting
 * - History expiration logic
 * - History deduplication
 *
 * @param searchHistoryRepository Repository for search history operations
 */
class ObserveSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    operator fun invoke() = searchHistoryRepository.observeHistory()
}

