package app.kwlee.weatherapp.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class GeocodingLocationDto(
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "local_names") val localNames: Map<String, String>?,
    @field:Json(name = "lat") val latitude: Double?,
    @field:Json(name = "lon") val longitude: Double?,
    @field:Json(name = "state") val state: String?,
    @field:Json(name = "country") val country: String?,
)

