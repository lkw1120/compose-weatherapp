package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ObserveFavoriteLocationsUseCaseTest {

    private lateinit var mockRepository: MockFavoriteRepository
    private lateinit var useCase: ObserveFavoriteLocationsUseCase

    @Before
    fun setup() {
        mockRepository = MockFavoriteRepository()
        useCase = ObserveFavoriteLocationsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow of favorite locations`() = runTest {
        val locations = listOf(
            createTestLocation("Seoul"),
            createTestLocation("Busan"),
        )
        mockRepository.setFavorites(locations)

        val result = useCase().first()

        assertEquals(locations, result)
    }

    @Test
    fun `invoke returns empty flow when no favorites`() = runTest {
        mockRepository.setFavorites(emptyList())

        val result = useCase().first()

        assertEquals(emptyList<FavoriteLocation>(), result)
    }

    private fun createTestLocation(name: String): FavoriteLocation {
        return FavoriteLocation(
            name = name,
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private class MockFavoriteRepository : FavoriteRepository {
        private var favorites: List<FavoriteLocation> = emptyList()

        fun setFavorites(favorites: List<FavoriteLocation>) {
            this.favorites = favorites
        }

        override fun observeFavorites() = flowOf(favorites)

        override suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean> {
            return Result.success(true)
        }

        override suspend fun isFavorite(location: FavoriteLocation): Result<Boolean> {
            return Result.success(false)
        }
    }
}

