package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class ToggleFavoriteLocationUseCaseTest {

    private lateinit var mockRepository: MockFavoriteRepository
    private lateinit var useCase: ToggleFavoriteLocationUseCase

    @Before
    fun setup() {
        mockRepository = MockFavoriteRepository()
        useCase = ToggleFavoriteLocationUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success true when location is added`() = runTest {
        val location = createTestLocation()
        mockRepository.setToggleResult(Result.success(true))

        val result = useCase(location)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        assertEquals(1, mockRepository.toggleFavoriteCallCount)
        assertEquals(location, mockRepository.lastLocation)
    }

    @Test
    fun `invoke returns success false when location is removed`() = runTest {
        val location = createTestLocation()
        mockRepository.setToggleResult(Result.success(false))

        val result = useCase(location)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull())
        assertEquals(1, mockRepository.toggleFavoriteCallCount)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val location = createTestLocation()
        val exception = Exception("Database error")
        mockRepository.setToggleResult(Result.failure(exception))

        val result = useCase(location)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private fun createTestLocation(): FavoriteLocation {
        return FavoriteLocation(
            name = "Seoul",
            latitude = 37.5665,
            longitude = 126.9780,
        )
    }

    private class MockFavoriteRepository : FavoriteRepository {
        private var toggleResult: Result<Boolean> = Result.success(true)
        var toggleFavoriteCallCount = 0
        var lastLocation: FavoriteLocation? = null

        fun setToggleResult(result: Result<Boolean>) {
            this.toggleResult = result
        }

        override suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean> {
            toggleFavoriteCallCount++
            lastLocation = location
            return toggleResult
        }

        override fun observeFavorites() = kotlinx.coroutines.flow.flowOf(emptyList<FavoriteLocation>())

        override suspend fun isFavorite(location: FavoriteLocation): Result<Boolean> {
            return Result.success(false)
        }
    }
}

