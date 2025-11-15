package app.kwlee.weatherapp.core.domain.model


data class AirPollution(
    val airQualityIndex: Int? = null,
    val co: Double? = null,
    val no: Double? = null,
    val no2: Double? = null,
    val o3: Double? = null,
    val so2: Double? = null,
    val pm25: Double? = null,
    val pm10: Double? = null,
    val nh3: Double? = null,
)

