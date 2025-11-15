package app.kwlee.weatherapp.core.domain.model


data class WeatherHighlight(
    val value: String,
    val type: HighlightType,
)


enum class HighlightType {
    FEELS_LIKE,
    HUMIDITY,
    PRECIPITATION,
    PRECIPITATION_PROBABILITY,
    SNOW,
    WIND,
    WIND_DIRECTION,
    UV_INDEX,
    PRESSURE,
    CLOUDINESS,
    VISIBILITY,
    SUNRISE,
    SUNSET,
    ALERT,
    AIR_QUALITY,
}

