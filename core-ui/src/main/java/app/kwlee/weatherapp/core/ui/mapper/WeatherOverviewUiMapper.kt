package app.kwlee.weatherapp.core.ui.mapper

import app.kwlee.weatherapp.core.domain.model.DailyForecast
import app.kwlee.weatherapp.core.domain.model.DistanceUnit
import app.kwlee.weatherapp.core.domain.model.HighlightType
import app.kwlee.weatherapp.core.domain.model.HourlyForecast
import app.kwlee.weatherapp.core.domain.model.PrecipitationUnit
import app.kwlee.weatherapp.core.domain.model.PressureUnit
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.TimeFormat
import app.kwlee.weatherapp.core.domain.model.WeatherAlert
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.domain.model.WeatherHighlight
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.ui.components.DailyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.HighlightUiType
import app.kwlee.weatherapp.core.ui.components.HourlyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherHighlightUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherIconType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import java.time.ZoneOffset
import com.lkw1120.easyformat.EasyFormat

data class WeatherOverviewUiModel(
    val locationLabel: String,
    val temperatureLabel: String,
    val conditionLabel: String,
    val summaryIcon: WeatherIconType,
    val summaryHighlights: List<WeatherHighlightUiModel>,
    val detailHighlights: List<WeatherHighlightUiModel>,
    val hourlyForecast: List<HourlyForecastUiModel>,
    val dailyForecast: List<DailyForecastUiModel>,
    val alerts: List<WeatherAlertUiModel>,
)

data class WeatherAlertUiModel(
    val event: String,
    val description: String,
    val start: Instant,
    val end: Instant,
)

@Singleton
class WeatherOverviewUiMapper @Inject constructor() {

    private val locale: Locale = Locale.getDefault()

    fun map(
        overview: WeatherOverview,
        settings: WeatherSettings,
        maxHourlyItems: Int = 24,
        maxDailyItems: Int = 8,
    ): WeatherOverviewUiModel {
        val cityName = overview.locationName.split(",").firstOrNull()?.trim() ?: overview.locationName
        return WeatherOverviewUiModel(
            locationLabel = cityName,
            temperatureLabel = formatTemperature(overview.temperatureCelsius, settings.temperatureUnit),
            conditionLabel = overview.conditionDescription,
            summaryIcon = overview.conditionType.toIconType(overview.conditionIcon),
            summaryHighlights = overview.highlights.toHighlightUiModels(
                overview = overview,
                settings = settings,
            ),
            detailHighlights = createDetailHighlights(overview, settings),
            hourlyForecast = overview.hourly
                .take(maxHourlyItems)
                .toHourlyUiModels(
                    timeFormat = settings.timeFormat,
                    temperatureUnit = settings.temperatureUnit,
                ),
            dailyForecast = overview.daily
                .take(maxDailyItems)
                .toDailyUiModels(settings.temperatureUnit),
            alerts = overview.alerts.toAlertUiModels(),
        )
    }

    private fun formatTemperature(value: Double, unit: TemperatureUnit): String {
        val (converted, suffix) = when (unit) {
            TemperatureUnit.CELSIUS -> value to "°C"
            TemperatureUnit.FAHRENHEIT -> celsiusToFahrenheit(value) to "°F"
        }
        return "${converted.roundToInt()}$suffix"
    }

    private fun formatPrecipitation(valueMm: Double, unit: PrecipitationUnit): String {
        return when (unit) {
            PrecipitationUnit.MILLIMETERS -> "${valueMm.roundToInt()}mm"
            PrecipitationUnit.CENTIMETERS -> formatMeasurement(valueMm / 10.0, "cm")
            PrecipitationUnit.INCHES -> formatMeasurement(valueMm / 25.4, "in")
        }
    }

    private fun formatSnow(valueMm: Double, unit: PrecipitationUnit): String {
        return when (unit) {
            PrecipitationUnit.MILLIMETERS -> "${valueMm.roundToInt()}mm"
            PrecipitationUnit.CENTIMETERS -> formatMeasurement(valueMm / 10.0, "cm")
            PrecipitationUnit.INCHES -> formatMeasurement(valueMm / 25.4, "in")
        }
    }

    private fun formatMeasurement(value: Double, suffix: String): String {
        val rounded = (value * 10).roundToInt() / 10.0
        val display = if (abs(rounded - rounded.toInt()) < 1e-4) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
        return "$display$suffix"
    }

    private fun formatHourLabel(dateTime: ZonedDateTime, format: TimeFormat): String {
        val suffix = getHourSuffix(locale)

        return when (format) {
            TimeFormat.TWENTY_FOUR_HOUR -> "${dateTime.hour}$suffix"
            TimeFormat.TWELVE_HOUR -> formatHourTwelve(dateTime, suffix)
        }
    }

    private fun formatHourTwelve(dateTime: ZonedDateTime, suffix: String): String {
        val hour24 = dateTime.hour
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        val period = getPeriodLabel(locale, hour24)
        return "$period $hour12$suffix"
    }

    private fun celsiusToFahrenheit(value: Double): Double = (value * 9.0 / 5.0) + 32.0

    private fun formatWind(
        speedMs: Double?,
        unit: WindSpeedUnit,
    ): String? {
        val speed = speedMs ?: return null
        return formatSpeed(speed, unit)
    }

    private fun formatWindOrDefault(
        speedMs: Double?,
        unit: WindSpeedUnit,
    ): String {
        return formatWind(speedMs, unit) ?: "0 ${getWindSpeedUnitSuffix(unit)}"
    }

    private fun formatSpeed(valueMs: Double, unit: WindSpeedUnit): String {
        val (converted, suffix) = when (unit) {
            WindSpeedUnit.METERS_PER_SECOND -> valueMs to "m/s"
            WindSpeedUnit.KILOMETERS_PER_HOUR -> valueMs * 3.6 to "km/h"
            WindSpeedUnit.MILES_PER_HOUR -> valueMs * 2.23694 to "mph"
        }
        return formatMeasurement(converted, suffix)
    }

    private fun formatWindDirection(degrees: Int): String {
        val normalized = ((degrees % 360) + 360) % 360
        val directions = listOf(
            "N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW",
        )
        val index = ((normalized / 22.5).roundToInt()) % directions.size
        return directions[index]
    }

    private fun formatWindDirectionLabel(
        compass: String?,
        degrees: Int?,
    ): String? {
        val direction = compass ?: degrees?.let(::formatWindDirection) ?: return null
        return if (degrees != null) "$direction (${degrees}°)" else direction
    }

    private fun formatUvIndex(uvIndex: Double?): String? {
        val index = uvIndex ?: return null
        val rounded = (index * 10).roundToInt() / 10.0
        val display = if (abs(rounded - rounded.toInt()) < 1e-4) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
        val rating = getUvIndexRating(locale, index)
        return "$display ($rating)"
    }

    private fun formatUvIndexOrDefault(uvIndex: Double?): String {
        return if (uvIndex != null) {
            formatUvIndex(uvIndex) ?: "0"
        } else {
            "0"
        }
    }

    private fun formatVisibility(
        visibilityMeters: Int?,
        unit: DistanceUnit,
    ): String? {
        val meters = visibilityMeters ?: return null
        return when (unit) {
            DistanceUnit.KILOMETERS -> formatMeasurement(meters / 1000.0, "km")
            DistanceUnit.MILES -> formatMeasurement(meters / 1609.34, "mi")
        }
    }

    private fun formatVisibilityOrDefault(
        visibilityMeters: Int?,
        unit: DistanceUnit,
    ): String {
        return formatVisibility(visibilityMeters, unit) ?: when (unit) {
            DistanceUnit.KILOMETERS -> "0 km"
            DistanceUnit.MILES -> "0 mi"
        }
    }

    private fun getWindSpeedUnitSuffix(unit: WindSpeedUnit): String {
        return when (unit) {
            WindSpeedUnit.METERS_PER_SECOND -> "m/s"
            WindSpeedUnit.KILOMETERS_PER_HOUR -> "km/h"
            WindSpeedUnit.MILES_PER_HOUR -> "mph"
        }
    }

    private fun formatPressure(
        pressureHpa: Int?,
        unit: PressureUnit,
    ): String? {
        val hpa = pressureHpa ?: return null
        return when (unit) {
            PressureUnit.HECTOPASCAL -> "$hpa hPa"
            PressureUnit.MILLIMETERS_OF_MERCURY -> {
                val mmHg = hpa * 0.750062
                "${mmHg.roundToInt()} mmHg"
            }
            PressureUnit.INCHES_OF_MERCURY -> {
                val inHg = hpa * 0.02953
                formatMeasurement(inHg, "inHg")
            }
        }
    }

    private fun formatPressureOrDefault(
        pressureHpa: Int?,
        unit: PressureUnit,
    ): String {
        return formatPressure(pressureHpa, unit) ?: when (unit) {
            PressureUnit.HECTOPASCAL -> "0 hPa"
            PressureUnit.MILLIMETERS_OF_MERCURY -> "0 mmHg"
            PressureUnit.INCHES_OF_MERCURY -> "0 inHg"
        }
    }

    private fun formatSunTime(
        instant: Instant?,
        timeFormat: TimeFormat,
        zoneId: ZoneId,
        offsetSeconds: Int,
    ): String? {
        val value = instant ?: return null
        val pattern = when (timeFormat) {
            TimeFormat.TWENTY_FOUR_HOUR -> "HH:mm"
            TimeFormat.TWELVE_HOUR -> "a h:mm"
        }
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        val zonedDateTime = runCatching { value.atZone(zoneId) }
            .getOrElse {
                value.atOffset(ZoneOffset.ofTotalSeconds(offsetSeconds)).toZonedDateTime()
            }
        return formatter.format(zonedDateTime)
    }

    private fun formatSunTimeOrDefault(
        instant: Instant?,
        timeFormat: TimeFormat,
        zoneId: ZoneId,
        offsetSeconds: Int,
    ): String {
        return formatSunTime(instant, timeFormat, zoneId, offsetSeconds) ?: "N/A"
    }

    private fun List<WeatherHighlight>.toHighlightUiModels(
        overview: WeatherOverview,
        settings: WeatherSettings,
    ): List<WeatherHighlightUiModel> {
        return map { highlight ->
            val value = when (highlight.type) {
                HighlightType.FEELS_LIKE -> formatTemperature(overview.feelsLikeCelsius, settings.temperatureUnit)
                HighlightType.PRECIPITATION -> {
                    if (overview.precipitationMm > 0.0) {
                        formatPrecipitation(overview.precipitationMm, settings.precipitationUnit)
                    } else {
                        highlight.value
                    }
                }
                HighlightType.PRECIPITATION_PROBABILITY -> highlight.value
                HighlightType.SNOW -> {
                    if (overview.snowMm > 0.0) {
                        formatSnow(overview.snowMm, settings.precipitationUnit)
                    } else {
                        highlight.value
                    }
                }
                HighlightType.WIND -> formatWind(
                    speedMs = overview.windSpeedMetersPerSecond,
                    unit = settings.windSpeedUnit,
                ) ?: highlight.value
                HighlightType.WIND_DIRECTION -> formatWindDirectionLabel(
                    compass = overview.windCompassDirection,
                    degrees = overview.windDirectionDegrees,
                ) ?: highlight.value
                HighlightType.PRESSURE -> formatPressure(
                    pressureHpa = overview.pressureHpa,
                    unit = settings.pressureUnit,
                ) ?: highlight.value
                HighlightType.UV_INDEX -> formatUvIndex(overview.uvIndex) ?: highlight.value
                HighlightType.CLOUDINESS -> overview.cloudinessPercent?.let { "$it%" } ?: highlight.value
                HighlightType.VISIBILITY -> formatVisibility(
                    visibilityMeters = overview.visibilityMeters,
                    unit = settings.distanceUnit,
                ) ?: highlight.value
                HighlightType.SUNRISE -> formatSunTime(
                    instant = overview.sunrise,
                    timeFormat = settings.timeFormat,
                    zoneId = overview.timeZone,
                    offsetSeconds = overview.timeZoneOffsetSeconds,
                ) ?: highlight.value
                HighlightType.SUNSET -> formatSunTime(
                    instant = overview.sunset,
                    timeFormat = settings.timeFormat,
                    zoneId = overview.timeZone,
                    offsetSeconds = overview.timeZoneOffsetSeconds,
                ) ?: highlight.value
                HighlightType.ALERT -> highlight.value
                HighlightType.AIR_QUALITY -> highlight.value
                else -> highlight.value
            }
            WeatherHighlightUiModel(
                type = highlight.type.toUiType(),
                value = value,
                icon = highlight.type.toIconType(),
            )
        }
    }

    private fun List<HourlyForecast>.toHourlyUiModels(
        timeFormat: TimeFormat,
        temperatureUnit: TemperatureUnit,
    ): List<HourlyForecastUiModel> = map { forecast ->
        HourlyForecastUiModel(
            timeLabel = formatHourLabel(forecast.dateTime, timeFormat),
            icon = forecast.condition.toIconType(forecast.conditionIcon),
            temperatureLabel = formatTemperature(forecast.temperatureCelsius, temperatureUnit),
        )
    }

    private fun List<DailyForecast>.toDailyUiModels(
        temperatureUnit: TemperatureUnit,
    ): List<DailyForecastUiModel> {
        val today = LocalDate.now()
        val todayLabel = getTodayLabel(locale)
        val tomorrowLabel = getTomorrowLabel(locale)
        return map { forecast ->
            val label = when (forecast.date) {
                today -> todayLabel
                today.plusDays(1) -> tomorrowLabel
                else -> {
                    EasyFormat.locale(locale).E().MMMd().format(forecast.date.atStartOfDay())
                }
            }
            DailyForecastUiModel(
                dayLabel = label,
                icon = forecast.condition.toIconType(forecast.conditionIcon),
                highTemperatureLabel = formatTemperature(forecast.highTemperatureCelsius, temperatureUnit),
                lowTemperatureLabel = formatTemperature(forecast.lowTemperatureCelsius, temperatureUnit),
            )
        }
    }

    private fun WeatherConditionType.toIconType(iconCode: String? = null): WeatherIconType {
        val isNight = iconCode?.endsWith("n") == true
        return when (this) {
            WeatherConditionType.CLEAR -> if (isNight) WeatherIconType.Moon else WeatherIconType.Sun
            WeatherConditionType.PARTLY_CLOUDY -> if (isNight) WeatherIconType.CloudMoon else WeatherIconType.CloudSun
            WeatherConditionType.CLOUDY -> WeatherIconType.Cloudy
            WeatherConditionType.RAIN -> WeatherIconType.Rain
            WeatherConditionType.THUNDERSTORM -> WeatherIconType.Rain
            WeatherConditionType.SNOW -> WeatherIconType.Snow
            WeatherConditionType.FOG -> WeatherIconType.Humidity
        }
    }

    private fun HighlightType.toUiType(): HighlightUiType = when (this) {
        HighlightType.FEELS_LIKE -> HighlightUiType.FEELS_LIKE
        HighlightType.HUMIDITY -> HighlightUiType.HUMIDITY
        HighlightType.PRECIPITATION -> HighlightUiType.PRECIPITATION
        HighlightType.PRECIPITATION_PROBABILITY -> HighlightUiType.PRECIPITATION_PROBABILITY
        HighlightType.SNOW -> HighlightUiType.SNOW
        HighlightType.WIND -> HighlightUiType.WIND
        HighlightType.WIND_DIRECTION -> HighlightUiType.WIND_DIRECTION
        HighlightType.UV_INDEX -> HighlightUiType.UV_INDEX
        HighlightType.PRESSURE -> HighlightUiType.PRESSURE
        HighlightType.CLOUDINESS -> HighlightUiType.CLOUDINESS
        HighlightType.VISIBILITY -> HighlightUiType.VISIBILITY
        HighlightType.SUNRISE -> HighlightUiType.SUNRISE
        HighlightType.SUNSET -> HighlightUiType.SUNSET
        HighlightType.ALERT -> HighlightUiType.ALERT
        HighlightType.AIR_QUALITY -> HighlightUiType.AIR_QUALITY
    }

    private fun HighlightType.toIconType(): WeatherIconType = when (this) {
        HighlightType.FEELS_LIKE -> WeatherIconType.Thermometer
        HighlightType.HUMIDITY -> WeatherIconType.Humidity
        HighlightType.PRECIPITATION -> WeatherIconType.Rain
        HighlightType.PRECIPITATION_PROBABILITY -> WeatherIconType.Rain
        HighlightType.SNOW -> WeatherIconType.Snow
        HighlightType.WIND -> WeatherIconType.Wind
        HighlightType.WIND_DIRECTION -> WeatherIconType.Compass
        HighlightType.UV_INDEX -> WeatherIconType.Sun
        HighlightType.PRESSURE -> WeatherIconType.Gauge
        HighlightType.CLOUDINESS -> WeatherIconType.Cloudy
        HighlightType.VISIBILITY -> WeatherIconType.Eye
        HighlightType.SUNRISE -> WeatherIconType.Sunrise
        HighlightType.SUNSET -> WeatherIconType.Sunset
        HighlightType.ALERT -> WeatherIconType.Alert
        HighlightType.AIR_QUALITY -> WeatherIconType.Humidity
    }

    private fun getTodayLabel(locale: Locale): String {
        return when (locale.language.lowercase(Locale.ROOT)) {
            "ko" -> "오늘"
            "en" -> "Today"
            "ja" -> "今日"
            "zh" -> if (locale.country.equals("TW", ignoreCase = true)) "今天" else "今天"
            "es" -> "Hoy"
            "fr" -> "Aujourd'hui"
            "de" -> "Heute"
            "it" -> "Oggi"
            "pt" -> "Hoje"
            "ru" -> "Сегодня"
            else -> "Today"
        }
    }

    private fun getTomorrowLabel(locale: Locale): String {
        return when (locale.language.lowercase(Locale.ROOT)) {
            "ko" -> "내일"
            "en" -> "Tomorrow"
            "ja" -> "明日"
            "zh" -> if (locale.country.equals("TW", ignoreCase = true)) "明天" else "明天"
            "es" -> "Mañana"
            "fr" -> "Demain"
            "de" -> "Morgen"
            "it" -> "Domani"
            "pt" -> "Amanhã"
            "ru" -> "Завтра"
            else -> "Tomorrow"
        }
    }

    private fun getHourSuffix(locale: Locale): String {
        return when (locale.language.lowercase(Locale.ROOT)) {
            "ko" -> "시"
            "ja" -> "時"
            "zh" -> "时"
            else -> "h"
        }
    }

    private fun getPeriodLabel(locale: Locale, hour24: Int): String {
        return when (locale.language.lowercase(Locale.ROOT)) {
            "ko" -> if (hour24 < 12) "오전" else "오후"
            "ja" -> if (hour24 < 12) "午前" else "午後"
            "zh" -> if (hour24 < 12) "上午" else "下午"
            else -> if (hour24 < 12) "AM" else "PM"
        }
    }

    private fun getUvIndexRating(locale: Locale, index: Double): String {
        val language = locale.language.lowercase(Locale.ROOT)
        return when {
            index < 3 -> when (language) {
                "ko" -> "낮음"
                "en" -> "Low"
                "ja" -> "低い"
                "zh" -> "低"
                "es" -> "Bajo"
                "fr" -> "Faible"
                "de" -> "Niedrig"
                "it" -> "Basso"
                "pt" -> "Baixo"
                "ru" -> "Низкий"
                else -> "Low"
            }
            index < 6 -> when (language) {
                "ko" -> "보통"
                "en" -> "Moderate"
                "ja" -> "普通"
                "zh" -> "中等"
                "es" -> "Moderado"
                "fr" -> "Modéré"
                "de" -> "Mäßig"
                "it" -> "Moderato"
                "pt" -> "Moderado"
                "ru" -> "Умеренный"
                else -> "Moderate"
            }
            index < 8 -> when (language) {
                "ko" -> "높음"
                "en" -> "High"
                "ja" -> "高い"
                "zh" -> "高"
                "es" -> "Alto"
                "fr" -> "Élevé"
                "de" -> "Hoch"
                "it" -> "Alto"
                "pt" -> "Alto"
                "ru" -> "Высокий"
                else -> "High"
            }
            index < 11 -> when (language) {
                "ko" -> "매우 높음"
                "en" -> "Very High"
                "ja" -> "非常に高い"
                "zh" -> "很高"
                "es" -> "Muy Alto"
                "fr" -> "Très Élevé"
                "de" -> "Sehr Hoch"
                "it" -> "Molto Alto"
                "pt" -> "Muito Alto"
                "ru" -> "Очень Высокий"
                else -> "Very High"
            }
            else -> when (language) {
                "ko" -> "위험"
                "en" -> "Extreme"
                "ja" -> "危険"
                "zh" -> "极端"
                "es" -> "Extremo"
                "fr" -> "Extrême"
                "de" -> "Extrem"
                "it" -> "Estremo"
                "pt" -> "Extremo"
                "ru" -> "Экстремальный"
                else -> "Extreme"
            }
        }
    }

    private fun List<WeatherAlert>.toAlertUiModels(): List<WeatherAlertUiModel> {
        return map { alert ->
            WeatherAlertUiModel(
                event = alert.event,
                description = alert.description,
                start = alert.start,
                end = alert.end,
            )
        }
    }

    private fun createDetailHighlights(
        overview: WeatherOverview,
        settings: WeatherSettings,
    ): List<WeatherHighlightUiModel> {
        return listOf(
            WeatherHighlightUiModel(
                type = HighlightUiType.SUNRISE,
                value = formatSunTimeOrDefault(
                    instant = overview.sunrise,
                    timeFormat = settings.timeFormat,
                    zoneId = overview.timeZone,
                    offsetSeconds = overview.timeZoneOffsetSeconds,
                ),
                icon = WeatherIconType.Sunrise,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.SUNSET,
                value = formatSunTimeOrDefault(
                    instant = overview.sunset,
                    timeFormat = settings.timeFormat,
                    zoneId = overview.timeZone,
                    offsetSeconds = overview.timeZoneOffsetSeconds,
                ),
                icon = WeatherIconType.Sunset,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.WIND,
                value = formatWindOrDefault(overview.windSpeedMetersPerSecond, settings.windSpeedUnit),
                icon = WeatherIconType.Wind,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.WIND_DIRECTION,
                value = formatWindDirectionLabel(
                    compass = overview.windCompassDirection,
                    degrees = overview.windDirectionDegrees,
                ) ?: "N/A",
                icon = WeatherIconType.Compass,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.PRESSURE,
                value = formatPressureOrDefault(overview.pressureHpa, settings.pressureUnit),
                icon = WeatherIconType.Gauge,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.UV_INDEX,
                value = formatUvIndexOrDefault(overview.uvIndex),
                icon = WeatherIconType.Sun,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.CLOUDINESS,
                value = overview.cloudinessPercent?.let { "$it%" } ?: "0%",
                icon = WeatherIconType.Cloudy,
            ),
            WeatherHighlightUiModel(
                type = HighlightUiType.VISIBILITY,
                value = formatVisibilityOrDefault(overview.visibilityMeters, settings.distanceUnit),
                icon = WeatherIconType.Eye,
            ),
        )
    }

}

