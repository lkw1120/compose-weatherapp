package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import javax.inject.Inject


/**
 * UseCase for removing a search query from history.
 *
 * Encapsulates the business logic for deleting search history entries.
 * Future enhancements could include:
 * - Bulk deletion
 * - Confirmation logic
 * - Analytics tracking
 *
 * @param searchHistoryRepository Repository for search history operations
 */
class RemoveSearchHistoryEntryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    suspend operator fun invoke(query: String): Result<Unit> {
        return searchHistoryRepository.removeQuery(query)
    }
}

