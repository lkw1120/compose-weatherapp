package app.kwlee.weatherapp.core.ui.components


enum class HighlightUiType {
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


data class HourlyForecastUiModel(
    val timeLabel: String,
    val icon: WeatherIconType,
    val temperatureLabel: String,
)


data class WeatherHighlightUiModel(
    val type: HighlightUiType,
    val value: String,
    val icon: WeatherIconType,
)


data class DailyForecastUiModel(
    val dayLabel: String,
    val icon: WeatherIconType,
    val highTemperatureLabel: String,
    val lowTemperatureLabel: String,
)

