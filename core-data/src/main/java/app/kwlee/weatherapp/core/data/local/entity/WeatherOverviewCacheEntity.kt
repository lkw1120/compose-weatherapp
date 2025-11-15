package app.kwlee.weatherapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "weather_overview_cache",
    primaryKeys = ["latitude", "longitude"],
    indices = [
        Index(value = ["updatedAtEpochMillis"]),
    ],
)
data class WeatherOverviewCacheEntity(
    val latitude: Double,
    val longitude: Double,
    val overviewJson: String,
    val updatedAtEpochMillis: Long,
)

