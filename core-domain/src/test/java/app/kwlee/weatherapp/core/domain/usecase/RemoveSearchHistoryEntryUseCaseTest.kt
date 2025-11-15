package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RemoveSearchHistoryEntryUseCaseTest {

    private lateinit var mockRepository: MockSearchHistoryRepository
    private lateinit var useCase: RemoveSearchHistoryEntryUseCase

    @Before
    fun setup() {
        mockRepository = MockSearchHistoryRepository()
        useCase = RemoveSearchHistoryEntryUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        mockRepository.setRemoveResult(Result.success(Unit))

        val result = useCase("Seoul")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        assertEquals(1, mockRepository.removeQueryCallCount)
        assertEquals("Seoul", mockRepository.lastQuery)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val exception = Exception("DataStore error")
        mockRepository.setRemoveResult(Result.failure(exception))

        val result = useCase("Seoul")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockSearchHistoryRepository : SearchHistoryRepository {
        private var removeResult: Result<Unit> = Result.success(Unit)
        var removeQueryCallCount = 0
        var lastQuery: String? = null

        fun setRemoveResult(result: Result<Unit>) {
            this.removeResult = result
        }

        override suspend fun removeQuery(query: String): Result<Unit> {
            removeQueryCallCount++
            lastQuery = query
            return removeResult
        }

        override suspend fun addQuery(query: String): Result<Unit> {
            return Result.success(Unit)
        }

        override fun observeHistory() = kotlinx.coroutines.flow.flowOf(emptyList<String>())
    }
}

