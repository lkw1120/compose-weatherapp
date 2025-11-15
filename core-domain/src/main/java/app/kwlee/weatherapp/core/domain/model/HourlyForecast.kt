package app.kwlee.weatherapp.core.domain.model

import java.time.ZonedDateTime


data class HourlyForecast(
    val dateTime: ZonedDateTime,
    val temperatureCelsius: Double,
    val condition: WeatherConditionType,
    val conditionIcon: String? = null,
    val precipitationProbability: Double? = null,
)

