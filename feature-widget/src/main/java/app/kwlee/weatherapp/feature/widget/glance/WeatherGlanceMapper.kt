package app.kwlee.weatherapp.feature.widget.glance

import android.content.Context
import androidx.annotation.DrawableRes
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.domain.model.DailyForecast
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import com.lkw1120.easyformat.EasyFormat

data class WeatherGlanceModel(
    val locationLabel: String,
    val temperatureLabel: String,
    val conditionLabel: String,
    val temperatureRangeLabel: String,
    val dailyItems: List<WeatherGlanceDailyItem>,
    @param:DrawableRes val iconRes: Int,
)

@Singleton
class WeatherGlanceMapper @Inject constructor() {

    fun map(
        overview: WeatherOverview,
        settings: WeatherSettings,
        context: Context,
        maxDailyItems: Int = 7,
    ): WeatherGlanceModel {
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        
        val temperatureLabel = formatTemperatureWithoutUnit(
            overview.temperatureCelsius,
            settings.temperatureUnit,
        )
        
        val todayForecast = overview.daily.firstOrNull()
        val temperatureRangeLabel = todayForecast?.let {
            formatTemperatureRange(
                context = context,
                highCelsius = it.highTemperatureCelsius,
                lowCelsius = it.lowTemperatureCelsius,
                unit = settings.temperatureUnit,
            )
        } ?: ""
        
        val dailyItems = overview.daily
            .drop(1)
            .take(maxDailyItems)
            .map { forecast ->
                WeatherGlanceDailyItem(
                    dayLabel = formatDayLabel(forecast.date, locale),
                    temperatureRangeLabel = formatTemperatureRange(
                        context = context,
                        highCelsius = forecast.highTemperatureCelsius,
                        lowCelsius = forecast.lowTemperatureCelsius,
                        unit = settings.temperatureUnit,
                    ),
                    iconRes = forecast.condition.toDrawableRes(forecast.conditionIcon),
                )
            }
        
        val cityName = overview.locationName.split(",").firstOrNull()?.trim() ?: overview.locationName
        val locationLabel = if (cityName.isNotBlank()) {
            cityName
        } else {
            context.getString(R.string.default_location_label)
        }
        return WeatherGlanceModel(
            locationLabel = locationLabel,
            temperatureLabel = temperatureLabel,
            conditionLabel = overview.conditionDescription,
            temperatureRangeLabel = temperatureRangeLabel,
            dailyItems = dailyItems,
            iconRes = overview.conditionType.toDrawableRes(overview.conditionIcon),
        )
    }

    private fun formatTemperatureWithoutUnit(
        valueCelsius: Double,
        unit: TemperatureUnit,
    ): String {
        val converted = when (unit) {
            TemperatureUnit.CELSIUS -> valueCelsius
            TemperatureUnit.FAHRENHEIT -> celsiusToFahrenheit(valueCelsius)
        }
        return "${converted.roundToInt()}°"
    }

    private fun formatTemperatureRange(
        context: Context,
        highCelsius: Double,
        lowCelsius: Double,
        unit: TemperatureUnit,
    ): String {
        val high = formatTemperatureWithoutUnit(highCelsius, unit)
        val low = formatTemperatureWithoutUnit(lowCelsius, unit)
        return context.getString(R.string.widget_high_low_format, high, low)
    }

    private fun formatDayLabel(date: LocalDate, locale: Locale): String {
        val today = LocalDate.now()
        return when (date) {
            today -> getTodayLabel(locale)
            today.plusDays(1) -> getTomorrowLabel(locale)
            else -> {
                EasyFormat.locale(locale).E().format(date.atStartOfDay())
            }
        }
    }

    private fun celsiusToFahrenheit(value: Double): Double = (value * 9.0 / 5.0) + 32.0

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
}

