package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ObserveSearchHistoryUseCaseTest {

    private lateinit var mockRepository: MockSearchHistoryRepository
    private lateinit var useCase: ObserveSearchHistoryUseCase

    @Before
    fun setup() {
        mockRepository = MockSearchHistoryRepository()
        useCase = ObserveSearchHistoryUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow of search history`() = runTest {
        val history = listOf("Seoul", "Busan", "Incheon")
        mockRepository.setHistory(history)

        val result = useCase().first()

        assertEquals(history, result)
    }

    @Test
    fun `invoke returns empty flow when no history`() = runTest {
        mockRepository.setHistory(emptyList())

        val result = useCase().first()

        assertEquals(emptyList<String>(), result)
    }

    private class MockSearchHistoryRepository : SearchHistoryRepository {
        private var history: List<String> = emptyList()

        fun setHistory(history: List<String>) {
            this.history = history
        }

        override fun observeHistory() = flowOf(history)

        override suspend fun addQuery(query: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun removeQuery(query: String): Result<Unit> {
            return Result.success(Unit)
        }
    }
}

