package app.kwlee.weatherapp.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class AirPollutionResponseDto(
    @field:Json(name = "coord") val coordinates: CoordinatesDto?,
    @field:Json(name = "list") val list: List<AirPollutionDataDto>?,
)


@JsonClass(generateAdapter = true)
data class CoordinatesDto(
    @field:Json(name = "lon") val longitude: Double?,
    @field:Json(name = "lat") val latitude: Double?,
)


@JsonClass(generateAdapter = true)
data class AirPollutionDataDto(
    @field:Json(name = "dt") val timestamp: Long?,
    @field:Json(name = "main") val main: AirQualityMainDto?,
    @field:Json(name = "components") val components: AirPollutionComponentsDto?,
)


@JsonClass(generateAdapter = true)
data class AirQualityMainDto(
    @field:Json(name = "aqi") val airQualityIndex: Int?,
)


@JsonClass(generateAdapter = true)
data class AirPollutionComponentsDto(
    @field:Json(name = "co") val co: Double?,
    @field:Json(name = "no") val no: Double?,
    @field:Json(name = "no2") val no2: Double?,
    @field:Json(name = "o3") val o3: Double?,
    @field:Json(name = "so2") val so2: Double?,
    @field:Json(name = "pm2_5") val pm25: Double?,
    @field:Json(name = "pm10") val pm10: Double?,
    @field:Json(name = "nh3") val nh3: Double?,
)

