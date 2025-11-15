package app.kwlee.weatherapp.core.domain.usecase

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class GetWeatherOverviewUseCaseTest {

    private lateinit var mockRepository: MockWeatherRepository
    private lateinit var useCase: GetWeatherOverviewUseCase

    @Before
    fun setup() {
        mockRepository = MockWeatherRepository()
        useCase = GetWeatherOverviewUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val location = createTestLocation()
        val expectedOverview = createTestWeatherOverview(location.name)

        mockRepository.setWeatherOverview(expectedOverview)

        val result = useCase(location)

        assertTrue(result.isSuccess)
        assertEquals(expectedOverview, result.getOrNull())
        assertEquals(1, mockRepository.fetchWeatherOverviewCallCount)
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val location = createTestLocation()
        val expectedException = Exception("Network error")
        mockRepository.setException(expectedException)

        val result = useCase(location)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        assertEquals(1, mockRepository.fetchWeatherOverviewCallCount)
    }

    @Test
    fun `invoke passes correct location to repository`() = runTest {
        val location = createTestLocation(name = "Seoul", latitude = 37.5665, longitude = 126.9780)
        mockRepository.setWeatherOverview(createTestWeatherOverview(location.name))

        useCase(location)

        assertEquals(location, mockRepository.lastLocation)
    }

    private fun createTestLocation(
        name: String = "Test Location",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
    ): FavoriteLocation {
        return FavoriteLocation(
            name = name,
            latitude = latitude,
            longitude = longitude,
        )
    }

    private fun createTestWeatherOverview(locationName: String): WeatherOverview {
        return WeatherOverview(
            locationName = locationName,
            temperatureCelsius = 20.0,
            feelsLikeCelsius = 19.0,
            conditionDescription = "Clear sky",
            conditionType = WeatherConditionType.CLEAR,
            humidityPercent = 50,
            precipitationMm = 0.0,
            highlights = emptyList(),
            hourly = emptyList(),
            daily = emptyList(),
        )
    }

    private class MockWeatherRepository : WeatherRepository {
        private var weatherOverview: WeatherOverview? = null
        private var exception: Throwable? = null
        var fetchWeatherOverviewCallCount = 0
        var lastLocation: FavoriteLocation? = null

        fun setWeatherOverview(overview: WeatherOverview) {
            this.weatherOverview = overview
            this.exception = null
        }

        fun setException(exception: Throwable) {
            this.exception = exception
            this.weatherOverview = null
        }

        override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<WeatherOverview> {
            fetchWeatherOverviewCallCount++
            lastLocation = location
            return if (exception != null) {
                Result.failure(exception!!)
            } else {
                Result.success(weatherOverview!!)
            }
        }

        override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> {
            return Result.success(emptyList())
        }

        override suspend fun clearCache(): Result<Unit> {
            return Result.success(Unit)
        }
    }
}

