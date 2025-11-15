package app.kwlee.weatherapp.core.domain.repository

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation


interface LocationRepository {
    suspend fun getCurrentLocation(): Result<FavoriteLocation>
}

