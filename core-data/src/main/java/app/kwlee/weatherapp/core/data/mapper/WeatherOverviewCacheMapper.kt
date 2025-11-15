package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.local.entity.WeatherOverviewCacheEntity
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import com.squareup.moshi.Moshi
import java.time.Instant

fun WeatherOverview.toCacheEntity(
    latitude: Double,
    longitude: Double,
    moshi: Moshi,
    updatedAtEpochMillis: Long,
): WeatherOverviewCacheEntity? {
    val adapter = moshi.adapter(WeatherOverview::class.java)
    val serializableOverview = copy(lastUpdatedAt = null)
    val json = runCatching { adapter.toJson(serializableOverview) }.getOrNull() ?: return null
    return WeatherOverviewCacheEntity(
        latitude = latitude,
        longitude = longitude,
        overviewJson = json,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}

fun WeatherOverviewCacheEntity.toDomain(moshi: Moshi): WeatherOverview? {
    val adapter = moshi.adapter(WeatherOverview::class.java)
    val overview = runCatching { adapter.fromJson(overviewJson) }.getOrNull() ?: return null
    return overview.copy(lastUpdatedAt = Instant.ofEpochMilli(updatedAtEpochMillis))
}

