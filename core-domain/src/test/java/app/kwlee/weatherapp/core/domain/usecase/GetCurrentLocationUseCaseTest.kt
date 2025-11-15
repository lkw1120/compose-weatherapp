package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.LocationPermissionMissingException
import app.kwlee.weatherapp.core.domain.model.LocationProviderDisabledException
import app.kwlee.weatherapp.core.domain.model.LocationResult
import app.kwlee.weatherapp.core.domain.repository.LocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class GetCurrentLocationUseCaseTest {

    private lateinit var mockRepository: MockLocationRepository
    private lateinit var useCase: GetCurrentLocationUseCase

    @Before
    fun setup() {
        mockRepository = MockLocationRepository()
        useCase = GetCurrentLocationUseCase(mockRepository)
    }

    @Test
    fun `invoke returns Success when repository returns success`() = runTest {
        val expectedLocation = createTestLocation()
        mockRepository.setLocation(expectedLocation)

        val result = useCase()

        assertTrue(result is LocationResult.Success)
        assertEquals(expectedLocation, (result as LocationResult.Success).location)
        assertEquals(1, mockRepository.getCurrentLocationCallCount)
    }

    @Test
    fun `invoke returns PermissionRequired when repository throws LocationPermissionMissingException`() = runTest {
        mockRepository.setException(LocationPermissionMissingException())

        val result = useCase()

        assertTrue(result is LocationResult.PermissionRequired)
        assertEquals(1, mockRepository.getCurrentLocationCallCount)
    }

    @Test
    fun `invoke returns ProviderDisabled when repository throws LocationProviderDisabledException`() = runTest {
        mockRepository.setException(LocationProviderDisabledException())

        val result = useCase()

        assertTrue(result is LocationResult.ProviderDisabled)
        assertEquals(1, mockRepository.getCurrentLocationCallCount)
    }

    @Test
    fun `invoke returns Error when repository throws other exception`() = runTest {
        val expectedException = Exception("Network error")
        mockRepository.setException(expectedException)

        val result = useCase()

        assertTrue(result is LocationResult.Error)
        assertEquals(expectedException, (result as LocationResult.Error).throwable)
        assertEquals(1, mockRepository.getCurrentLocationCallCount)
    }

    private fun createTestLocation(): FavoriteLocation {
        return FavoriteLocation(
            name = "Test Location",
            latitude = 37.5665,
            longitude = 126.9780,
            isPrimary = true,
        )
    }

    private class MockLocationRepository : LocationRepository {
        private var location: FavoriteLocation? = null
        private var exception: Throwable? = null
        var getCurrentLocationCallCount = 0

        fun setLocation(location: FavoriteLocation) {
            this.location = location
            this.exception = null
        }

        fun setException(exception: Throwable) {
            this.exception = exception
            this.location = null
        }

        override suspend fun getCurrentLocation(): Result<FavoriteLocation> {
            getCurrentLocationCallCount++
            return if (exception != null) {
                Result.failure(exception!!)
            } else {
                Result.success(location!!)
            }
        }
    }
}

