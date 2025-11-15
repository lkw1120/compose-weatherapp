package app.kwlee.weatherapp.core.domain.model

import java.time.LocalDate


data class DailyForecast(
    val date: LocalDate,
    val highTemperatureCelsius: Double,
    val lowTemperatureCelsius: Double,
    val condition: WeatherConditionType,
    val conditionIcon: String? = null,
    val precipitationProbability: Double? = null,
)

