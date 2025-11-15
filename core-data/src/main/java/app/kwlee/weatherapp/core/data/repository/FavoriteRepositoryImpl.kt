package app.kwlee.weatherapp.core.data.repository

import app.kwlee.weatherapp.core.data.local.dao.FavoriteLocationDao
import app.kwlee.weatherapp.core.data.mapper.toDomain
import app.kwlee.weatherapp.core.data.mapper.toEntity
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import app.kwlee.weatherapp.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject


class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteLocationDao: FavoriteLocationDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoriteRepository {

    override fun observeFavorites(): Flow<List<FavoriteLocation>> {
        return favoriteLocationDao.observeFavoriteLocations()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatching {
                val existing = favoriteLocationDao.findByCoordinates(location.latitude, location.longitude)
                return@runCatching if (existing != null) {
                    favoriteLocationDao.delete(existing)
                    false
                } else {
                    favoriteLocationDao.upsert(location.toEntity())
                    true
                }
            }
        }

    override suspend fun isFavorite(location: FavoriteLocation): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatching {
                favoriteLocationDao.findByCoordinates(location.latitude, location.longitude) != null
            }
        }
}

