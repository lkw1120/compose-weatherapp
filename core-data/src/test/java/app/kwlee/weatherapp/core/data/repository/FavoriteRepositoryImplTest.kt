package app.kwlee.weatherapp.core.data.repository

import app.kwlee.weatherapp.core.data.local.dao.FavoriteLocationDao
import app.kwlee.weatherapp.core.data.local.entity.FavoriteLocationEntity
import app.kwlee.weatherapp.core.data.mapper.toDomain
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class FavoriteRepositoryImplTest {

    private lateinit var mockDao: MockFavoriteLocationDao
    private lateinit var repository: FavoriteRepositoryImpl
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setup() {
        mockDao = MockFavoriteLocationDao()
        repository = FavoriteRepositoryImpl(mockDao, testDispatcher)
    }

    @Test
    fun `observeFavorites returns flow of favorite locations`() = runTest(testDispatcher) {
        val entity1 = createTestEntity(id = 1, name = "Seoul")
        val entity2 = createTestEntity(id = 2, name = "Busan")
        mockDao.setFavorites(listOf(entity1, entity2))

        val result = repository.observeFavorites().first()

        assertEquals(2, result.size)
        assertEquals(entity1.toDomain(), result[0])
        assertEquals(entity2.toDomain(), result[1])
    }

    @Test
    fun `toggleFavorite adds location when not exists`() = runTest(testDispatcher) {
        val location = createTestLocation(name = "Seoul")
        mockDao.setExistingLocation(null)

        val result = repository.toggleFavorite(location)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        assertEquals(1, mockDao.upsertCallCount)
        assertEquals(0, mockDao.deleteCallCount)
    }

    @Test
    fun `toggleFavorite removes location when exists`() = runTest(testDispatcher) {
        val location = createTestLocation(name = "Seoul")
        val existingEntity = createTestEntity(id = 1, name = "Seoul")
        mockDao.setExistingLocation(existingEntity)

        val result = repository.toggleFavorite(location)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull())
        assertEquals(0, mockDao.upsertCallCount)
        assertEquals(1, mockDao.deleteCallCount)
    }

    @Test
    fun `toggleFavorite returns failure when dao throws exception`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val exception = Exception("Database error")
        mockDao.setException(exception)

        val result = repository.toggleFavorite(location)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `isFavorite returns true when location exists`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val existingEntity = createTestEntity(id = 1)
        mockDao.setExistingLocation(existingEntity)

        val result = repository.isFavorite(location)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        assertEquals(1, mockDao.findByCoordinatesCallCount)
    }

    @Test
    fun `isFavorite returns false when location does not exist`() = runTest(testDispatcher) {
        val location = createTestLocation()
        mockDao.setExistingLocation(null)

        val result = repository.isFavorite(location)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull())
        assertEquals(1, mockDao.findByCoordinatesCallCount)
    }

    @Test
    fun `isFavorite returns failure when dao throws exception`() = runTest(testDispatcher) {
        val location = createTestLocation()
        val exception = Exception("Database error")
        mockDao.setException(exception)

        val result = repository.isFavorite(location)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private fun createTestLocation(
        id: Long? = null,
        name: String = "Test Location",
        latitude: Double = 37.5665,
        longitude: Double = 126.9780,
        isPrimary: Boolean = false,
    ): FavoriteLocation {
        return FavoriteLocation(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            isPrimary = isPrimary,
        )
    }

    private fun createTestEntity(
        id: Long = 0,
        name: String = "Test Location",
        latitude: Double = 37.5665,
        longitude: Double = 126.9780,
        isPrimary: Boolean = false,
    ): FavoriteLocationEntity {
        return FavoriteLocationEntity(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            isPrimary = isPrimary,
        )
    }

    private class MockFavoriteLocationDao : FavoriteLocationDao {
        private var favorites: List<FavoriteLocationEntity> = emptyList()
        private var existingLocation: FavoriteLocationEntity? = null
        private var exception: Throwable? = null
        var upsertCallCount = 0
        var deleteCallCount = 0
        var findByCoordinatesCallCount = 0

        fun setFavorites(favorites: List<FavoriteLocationEntity>) {
            this.favorites = favorites
        }

        fun setExistingLocation(location: FavoriteLocationEntity?) {
            this.existingLocation = location
        }

        fun setException(exception: Throwable) {
            this.exception = exception
        }

        override fun observeFavoriteLocations() = flowOf(favorites)

        override suspend fun upsert(location: FavoriteLocationEntity) {
            upsertCallCount++
            if (exception != null) throw exception!!
        }

        override suspend fun delete(location: FavoriteLocationEntity) {
            deleteCallCount++
            if (exception != null) throw exception!!
        }

        override suspend fun findByCoordinates(latitude: Double, longitude: Double): FavoriteLocationEntity? {
            findByCoordinatesCallCount++
            if (exception != null) throw exception!!
            return existingLocation
        }
    }
}

