package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class SearchLocationsUseCaseTest {

    private lateinit var mockRepository: MockWeatherRepository
    private lateinit var useCase: SearchLocationsUseCase

    @Before
    fun setup() {
        mockRepository = MockWeatherRepository()
        useCase = SearchLocationsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns empty list when query is blank`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isSuccess)
        assertEquals(emptyList<FavoriteLocation>(), result.getOrNull())
        assertEquals(0, mockRepository.searchLocationsCallCount)
    }

    @Test
    fun `invoke returns empty list when query is empty`() = runTest {
        val result = useCase("")

        assertTrue(result.isSuccess)
        assertEquals(emptyList<FavoriteLocation>(), result.getOrNull())
        assertEquals(0, mockRepository.searchLocationsCallCount)
    }

    @Test
    fun `invoke trims query before searching`() = runTest {
        val locations = listOf(createTestLocation("Seoul"))
        mockRepository.setSearchResult(Result.success(locations))

        val result = useCase("  Seoul  ")

        assertTrue(result.isSuccess)
        assertEquals(locations, result.getOrNull())
        assertEquals("Seoul", mockRepository.lastQuery)
    }

    @Test
    fun `invoke passes default limit of 5`() = runTest {
        mockRepository.setSearchResult(Result.success(emptyList()))

        useCase("Seoul")

        assertEquals(5, mockRepository.lastLimit)
    }

    @Test
    fun `invoke passes custom limit`() = runTest {
        mockRepository.setSearchResult(Result.success(emptyList()))

        useCase("Seoul", limit = 10)

        assertEquals(10, mockRepository.lastLimit)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val locations = listOf(
            createTestLocation("Seoul"),
            createTestLocation("Busan"),
        )
        mockRepository.setSearchResult(Result.success(locations))

        val result = useCase("Seoul")

        assertTrue(result.isSuccess)
        assertEquals(locations, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val exception = Exception("Network error")
        mockRepository.setSearchResult(Result.failure(exception))

        val result = useCase("Seoul")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private fun createTestLocation(name: String): FavoriteLocation {
        return FavoriteLocation(
            name = name,
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private class MockWeatherRepository : WeatherRepository {
        private var searchResult: Result<List<FavoriteLocation>> = Result.success(emptyList())
        var searchLocationsCallCount = 0
        var lastQuery: String? = null
        var lastLimit: Int? = null

        fun setSearchResult(result: Result<List<FavoriteLocation>>) {
            this.searchResult = result
        }

        override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> {
            searchLocationsCallCount++
            lastQuery = query
            lastLimit = limit
            return searchResult
        }

        override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<app.kwlee.weatherapp.core.domain.model.WeatherOverview> {
            return Result.failure(Exception("Not implemented"))
        }

        override suspend fun clearCache(): Result<Unit> {
            return Result.success(Unit)
        }
    }
}

