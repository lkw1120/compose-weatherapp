package app.kwlee.weatherapp.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class OneCallResponseDto(
    @field:Json(name = "timezone") val timezone: String?,
    @field:Json(name = "timezone_offset") val timezoneOffsetSeconds: Long?,
    @field:Json(name = "current") val current: CurrentWeatherDto?,
    @field:Json(name = "hourly") val hourly: List<HourlyWeatherDto>?,
    @field:Json(name = "daily") val daily: List<DailyWeatherDto>?,
    @field:Json(name = "alerts") val alerts: List<AlertDto>?,
)


@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @field:Json(name = "temp") val temperature: Double?,
    @field:Json(name = "feels_like") val feelsLike: Double?,
    @field:Json(name = "humidity") val humidity: Int?,
    @field:Json(name = "pressure") val pressure: Int?,
    @field:Json(name = "wind_speed") val windSpeed: Double?,
    @field:Json(name = "wind_deg") val windDegrees: Int?,
    @field:Json(name = "wind_gust") val windGust: Double?,
    @field:Json(name = "uvi") val uvIndex: Double?,
    @field:Json(name = "clouds") val cloudiness: Int?,
    @field:Json(name = "visibility") val visibility: Int?,
    @field:Json(name = "sunrise") val sunrise: Long?,
    @field:Json(name = "sunset") val sunset: Long?,
    @field:Json(name = "weather") val weather: List<WeatherDescriptionDto>?,
    @field:Json(name = "rain") val rain: Map<String, Double>?,
    @field:Json(name = "snow") val snow: Map<String, Double>?,
)


@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    @field:Json(name = "dt") val timestamp: Long?,
    @field:Json(name = "temp") val temperature: Double?,
    @field:Json(name = "weather") val weather: List<WeatherDescriptionDto>?,
    @field:Json(name = "pop") val precipitationProbability: Double?,
)


@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @field:Json(name = "dt") val timestamp: Long?,
    @field:Json(name = "temp") val temperature: DailyTemperatureDto?,
    @field:Json(name = "weather") val weather: List<WeatherDescriptionDto>?,
    @field:Json(name = "rain") val rain: Double?,
    @field:Json(name = "snow") val snow: Double?,
    @field:Json(name = "pop") val precipitationProbability: Double?,
)


@JsonClass(generateAdapter = true)
data class DailyTemperatureDto(
    @field:Json(name = "max") val high: Double?,
    @field:Json(name = "min") val low: Double?,
)


@JsonClass(generateAdapter = true)
data class WeatherDescriptionDto(
    @field:Json(name = "main") val main: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "icon") val icon: String?,
)


@JsonClass(generateAdapter = true)
data class AlertDto(
    @field:Json(name = "sender_name") val senderName: String?,
    @field:Json(name = "event") val event: String?,
    @field:Json(name = "start") val start: Long?,
    @field:Json(name = "end") val end: Long?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "tags") val tags: List<String>?,
)

