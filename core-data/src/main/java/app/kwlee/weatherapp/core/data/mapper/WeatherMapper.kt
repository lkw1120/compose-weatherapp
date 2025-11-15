package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.remote.dto.AlertDto
import app.kwlee.weatherapp.core.domain.model.WeatherAlert
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionComponentsDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionDataDto
import app.kwlee.weatherapp.core.data.remote.dto.AirPollutionResponseDto
import app.kwlee.weatherapp.core.data.remote.dto.DailyWeatherDto
import app.kwlee.weatherapp.core.data.remote.dto.HourlyWeatherDto
import app.kwlee.weatherapp.core.data.remote.dto.OneCallResponseDto
import app.kwlee.weatherapp.core.domain.model.AirPollution
import app.kwlee.weatherapp.core.domain.model.DailyForecast
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.HighlightType
import app.kwlee.weatherapp.core.domain.model.HourlyForecast
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.domain.model.WeatherHighlight
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.common.AppLogger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.roundToInt


fun OneCallResponseDto.toDomain(location: FavoriteLocation, airPollution: AirPollution? = null): WeatherOverview? {
    val currentWeather = current ?: return null
    val offsetSeconds = (timezoneOffsetSeconds ?: 0).toInt()
    val fallbackZone = ZoneOffset.ofTotalSeconds(offsetSeconds)
    val timezoneId = timezone
        ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
        ?: fallbackZone

    val condition = currentWeather.weather?.firstOrNull()
    val conditionType = mapCondition(condition?.icon, condition?.main, condition?.description)
    val description = condition?.description ?: ""

    val hourlyForecast = hourly.orEmpty()
        .take(24)
        .mapNotNull { it.toDomain(timezoneId) }
        .sortedBy { it.dateTime }

    val dailyForecast = daily.orEmpty()
        .take(8)
        .mapNotNull { it.toDomain(timezoneId) }
        .sortedBy { it.date }

    val rawPrecipitation = currentWeather.rain?.values?.firstOrNull() ?: 0.0
    val precipitation = if (rawPrecipitation > 0.0) rawPrecipitation else 0.0
    val rawSnow = currentWeather.snow?.values?.firstOrNull() ?: 0.0
    val snow = if (rawSnow > 0.0) rawSnow else 0.0
    val alerts = this@toDomain.alerts?.mapNotNull { it.toDomain() } ?: emptyList()
    AppLogger.d("Alerts count: ${alerts.size}, raw alerts: ${this@toDomain.alerts?.size ?: 0}")
    val currentPrecipitationProbability = hourlyForecast.firstOrNull()?.precipitationProbability

    val highlights = buildList {
        currentWeather.feelsLike?.let {
            add(
                WeatherHighlight(
                    value = formatTemperature(it),
                    type = HighlightType.FEELS_LIKE,
                ),
            )
        }

        currentWeather.humidity?.let {
            add(
                WeatherHighlight(
                    value = "$it%",
                    type = HighlightType.HUMIDITY,
                ),
            )
        }

        when {
            snow > 0.0 -> {
                val snowLabel = "${snow.roundToInt()}mm"
                add(
                    WeatherHighlight(
                        value = snowLabel,
                        type = HighlightType.SNOW,
                    ),
                )
            }
            precipitation > 0.0 -> {
                val precipitationLabel = "${precipitation.roundToInt()}mm"
                add(
                    WeatherHighlight(
                        value = precipitationLabel,
                        type = HighlightType.PRECIPITATION,
                    ),
                )
            }
            else -> {
                val probability = currentPrecipitationProbability ?: 0.0
                val probabilityPercent = (probability * 100).roundToInt()
                val probabilityLabel = "$probabilityPercent%"
                add(
                    WeatherHighlight(
                        value = probabilityLabel,
                        type = HighlightType.PRECIPITATION_PROBABILITY,
                    ),
                )
            }
        }

    }

    return WeatherOverview(
        locationName = location.name,
        temperatureCelsius = currentWeather.temperature ?: 0.0,
        feelsLikeCelsius = currentWeather.feelsLike ?: currentWeather.temperature ?: 0.0,
        conditionDescription = description,
        conditionType = conditionType,
        conditionIcon = condition?.icon,
        humidityPercent = currentWeather.humidity ?: 0,
        precipitationMm = precipitation,
        snowMm = snow,
        highlights = highlights,
        hourly = hourlyForecast,
        daily = dailyForecast,
        windSpeedMetersPerSecond = currentWeather.windSpeed,
        windDirectionDegrees = currentWeather.windDegrees,
        windGustMetersPerSecond = currentWeather.windGust,
        pressureHpa = currentWeather.pressure,
        uvIndex = currentWeather.uvIndex,
        cloudinessPercent = currentWeather.cloudiness,
        visibilityMeters = currentWeather.visibility,
        windCompassDirection = currentWeather.windDegrees?.let(::mapDirectionToCompass),
        sunrise = currentWeather.sunrise?.let { Instant.ofEpochSecond(it) },
        sunset = currentWeather.sunset?.let { Instant.ofEpochSecond(it) },
        airPollution = airPollution,
        alerts = alerts,
        timeZone = timezoneId,
        timeZoneOffsetSeconds = offsetSeconds,
        lastUpdatedAt = Instant.now(),
    )
}

private fun AlertDto.toDomain(): WeatherAlert? {
    val event = event ?: run {
        AppLogger.d("AlertDto.toDomain: event is null")
        return null
    }
    val start = start ?: run {
        AppLogger.d("AlertDto.toDomain: start is null")
        return null
    }
    val end = end ?: run {
        AppLogger.d("AlertDto.toDomain: end is null")
        return null
    }
    val description = description ?: run {
        AppLogger.d("AlertDto.toDomain: description is null")
        return null
    }
    
    AppLogger.d("AlertDto.toDomain: success - event=$event, start=$start, end=$end")
    return WeatherAlert(
        senderName = senderName,
        event = event,
        start = Instant.ofEpochSecond(start),
        end = Instant.ofEpochSecond(end),
        description = description,
        tags = tags,
    )
}


private fun HourlyWeatherDto.toDomain(zoneId: ZoneId): HourlyForecast? {
    val timestamp = timestamp ?: return null
    val temp = temperature ?: return null
    val condition = weather?.firstOrNull()
    return HourlyForecast(
        dateTime = timestamp.toZonedDateTime(zoneId),
        temperatureCelsius = temp,
        condition = mapCondition(condition?.icon, condition?.main, condition?.description),
        conditionIcon = condition?.icon,
        precipitationProbability = precipitationProbability,
    )
}


private fun DailyWeatherDto.toDomain(zoneId: ZoneId): DailyForecast? {
    val timestamp = timestamp ?: return null
    val high = temperature?.high ?: return null
    val low = temperature?.low ?: return null
    val condition = weather?.firstOrNull()
    return DailyForecast(
        date = timestamp.toZonedDateTime(zoneId).toLocalDate(),
        highTemperatureCelsius = high,
        lowTemperatureCelsius = low,
        condition = mapCondition(condition?.icon, condition?.main, condition?.description),
        conditionIcon = condition?.icon,
        precipitationProbability = precipitationProbability,
    )
}


private fun Long.toZonedDateTime(zoneId: ZoneId): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(this), zoneId)


private fun formatTemperature(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return String.format("%.1f°", rounded)
}


private fun mapCondition(iconRaw: String?, mainRaw: String?, descriptionRaw: String?): WeatherConditionType {
    val icon = iconRaw?.lowercase(Locale.US)
    val main = mainRaw?.lowercase(Locale.US)
    val description = descriptionRaw?.lowercase(Locale.US)

    val partlyCloudyDescriptions = setOf(
        "few clouds",
        "scattered clouds",
        "broken clouds",
    )

    return when {
        icon != null -> {
            when {
                icon.startsWith("01") -> WeatherConditionType.CLEAR
                icon.startsWith("02") -> WeatherConditionType.PARTLY_CLOUDY
                icon.startsWith("03") -> WeatherConditionType.PARTLY_CLOUDY
                icon.startsWith("04") -> WeatherConditionType.CLOUDY
                icon.startsWith("09") -> WeatherConditionType.RAIN
                icon.startsWith("10") -> WeatherConditionType.RAIN
                icon.startsWith("11") -> WeatherConditionType.THUNDERSTORM
                icon.startsWith("13") -> WeatherConditionType.SNOW
                icon.startsWith("50") -> WeatherConditionType.FOG
                else -> mapConditionFromMainAndDescription(main, description, partlyCloudyDescriptions)
            }
        }
        else -> mapConditionFromMainAndDescription(main, description, partlyCloudyDescriptions)
    }
}

private fun mapConditionFromMainAndDescription(
    main: String?,
    description: String?,
    partlyCloudyDescriptions: Set<String>,
): WeatherConditionType {
    return when {
        main == "clear" -> WeatherConditionType.CLEAR
        main == "clouds" && description in partlyCloudyDescriptions -> WeatherConditionType.PARTLY_CLOUDY
        main == "clouds" -> WeatherConditionType.CLOUDY
        main == "rain" || main == "drizzle" -> WeatherConditionType.RAIN
        main == "thunderstorm" || main == "squall" || main == "tornado" -> WeatherConditionType.THUNDERSTORM
        main == "snow" -> WeatherConditionType.SNOW
        main in setOf("mist", "fog", "haze", "smoke", "dust", "sand", "ash") -> WeatherConditionType.FOG
        description in partlyCloudyDescriptions -> WeatherConditionType.PARTLY_CLOUDY
        else -> WeatherConditionType.CLOUDY
    }
}

private fun mapDirectionToCompass(degrees: Int): String {
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

