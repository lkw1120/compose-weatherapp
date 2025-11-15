package app.kwlee.weatherapp.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ReverseGeocodingDto(
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "local_names") val localNames: Map<String, String>?,
    @field:Json(name = "state") val state: String?,
    @field:Json(name = "country") val country: String?,
)

