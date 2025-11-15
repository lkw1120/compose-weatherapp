package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import javax.inject.Inject


/**
 * UseCase for adding a search query to history.
 *
 * Encapsulates the business logic for recording search queries.
 * Future enhancements could include:
 * - Query validation/normalization
 * - Duplicate detection
 * - History size limits
 * - Analytics tracking
 *
 * @param searchHistoryRepository Repository for search history operations
 */
class AddSearchHistoryEntryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    suspend operator fun invoke(query: String): Result<Unit> {
        return searchHistoryRepository.addQuery(query)
    }
}

