package app.kwlee.weatherapp.core.domain.repository

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import kotlinx.coroutines.flow.Flow


interface FavoriteRepository {
    fun observeFavorites(): Flow<List<FavoriteLocation>>
    suspend fun toggleFavorite(location: FavoriteLocation): Result<Boolean>
    suspend fun isFavorite(location: FavoriteLocation): Result<Boolean>
}

