package app.kwlee.weatherapp.core.domain.repository

import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherOverview


interface WeatherRepository {
    suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<WeatherOverview>
    suspend fun searchLocations(query: String, limit: Int = 5): Result<List<FavoriteLocation>>
    suspend fun clearCache(): Result<Unit>
}

