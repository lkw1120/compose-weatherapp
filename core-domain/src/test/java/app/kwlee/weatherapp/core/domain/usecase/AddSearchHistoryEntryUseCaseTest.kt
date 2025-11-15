package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class AddSearchHistoryEntryUseCaseTest {

    private lateinit var mockRepository: MockSearchHistoryRepository
    private lateinit var useCase: AddSearchHistoryEntryUseCase

    @Before
    fun setup() {
        mockRepository = MockSearchHistoryRepository()
        useCase = AddSearchHistoryEntryUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        mockRepository.setAddResult(Result.success(Unit))

        val result = useCase("Seoul")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        assertEquals(1, mockRepository.addQueryCallCount)
        assertEquals("Seoul", mockRepository.lastQuery)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val exception = Exception("DataStore error")
        mockRepository.setAddResult(Result.failure(exception))

        val result = useCase("Seoul")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockSearchHistoryRepository : SearchHistoryRepository {
        private var addResult: Result<Unit> = Result.success(Unit)
        var addQueryCallCount = 0
        var lastQuery: String? = null

        fun setAddResult(result: Result<Unit>) {
            this.addResult = result
        }

        override suspend fun addQuery(query: String): Result<Unit> {
            addQueryCallCount++
            lastQuery = query
            return addResult
        }

        override fun observeHistory() = kotlinx.coroutines.flow.flowOf(emptyList<String>())

        override suspend fun removeQuery(query: String): Result<Unit> {
            return Result.success(Unit)
        }
    }
}

