package app.kwlee.weatherapp.core.domain.model

import java.time.Instant


data class WeatherAlert(
    val senderName: String?,
    val event: String,
    val start: Instant,
    val end: Instant,
    val description: String,
    val tags: List<String>?,
)

