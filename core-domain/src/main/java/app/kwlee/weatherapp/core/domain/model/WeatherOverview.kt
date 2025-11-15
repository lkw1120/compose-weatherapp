package app.kwlee.weatherapp.core.domain.model

import java.time.Instant
import java.time.ZoneId

data class WeatherOverview(
    val locationName: String,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val conditionDescription: String,
    val conditionType: WeatherConditionType,
    val conditionIcon: String? = null,
    val humidityPercent: Int,
    val precipitationMm: Double,
    val snowMm: Double = 0.0,
    val highlights: List<WeatherHighlight>,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val windSpeedMetersPerSecond: Double? = null,
    val windDirectionDegrees: Int? = null,
    val windGustMetersPerSecond: Double? = null,
    val pressureHpa: Int? = null,
    val uvIndex: Double? = null,
    val cloudinessPercent: Int? = null,
    val visibilityMeters: Int? = null,
    val windCompassDirection: String? = null,
    val sunrise: Instant? = null,
    val sunset: Instant? = null,
    val airPollution: AirPollution? = null,
    val alerts: List<WeatherAlert> = emptyList(),
    val timeZone: ZoneId = ZoneId.systemDefault(),
    val timeZoneOffsetSeconds: Int = timeZone.rules.getOffset(Instant.now()).totalSeconds,
    val lastUpdatedAt: Instant? = null,
)

