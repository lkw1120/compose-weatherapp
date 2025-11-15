package app.kwlee.weatherapp.feature.widget.glance

import androidx.annotation.DrawableRes
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.domain.model.WeatherConditionType
import app.kwlee.weatherapp.core.ui.components.WeatherIconType

@DrawableRes
fun WeatherIconType.toDrawableRes(): Int = when (this) {
    WeatherIconType.Sun -> R.drawable.ic_sun
    WeatherIconType.Moon -> R.drawable.ic_moon_star
    WeatherIconType.Cloud -> R.drawable.ic_cloud
    WeatherIconType.CloudSun -> R.drawable.ic_cloud_sun
    WeatherIconType.CloudMoon -> R.drawable.ic_cloud_moon
    WeatherIconType.Cloudy -> R.drawable.ic_cloudy
    WeatherIconType.Rain -> R.drawable.ic_cloud_rain
    WeatherIconType.Snow -> R.drawable.ic_snowflake
    WeatherIconType.Humidity -> R.drawable.ic_droplets
    WeatherIconType.Thermometer -> R.drawable.ic_thermometer
    WeatherIconType.Wind -> R.drawable.ic_wind
    WeatherIconType.Gauge -> R.drawable.ic_gauge
    WeatherIconType.Compass -> R.drawable.ic_compass
    WeatherIconType.Eye -> R.drawable.ic_eye
    WeatherIconType.Sunrise -> R.drawable.ic_sunrise
    WeatherIconType.Sunset -> R.drawable.ic_sunset
    WeatherIconType.Alert -> R.drawable.ic_cloud_alert
}

@DrawableRes
fun WeatherConditionType.toDrawableRes(iconCode: String? = null): Int {
    val isNight = iconCode?.endsWith("n") == true
    return when (this) {
        WeatherConditionType.CLEAR -> if (isNight) WeatherIconType.Moon.toDrawableRes() else WeatherIconType.Sun.toDrawableRes()
        WeatherConditionType.PARTLY_CLOUDY -> if (isNight) WeatherIconType.CloudMoon.toDrawableRes() else WeatherIconType.CloudSun.toDrawableRes()
        WeatherConditionType.CLOUDY -> WeatherIconType.Cloudy.toDrawableRes()
        WeatherConditionType.RAIN -> WeatherIconType.Rain.toDrawableRes()
        WeatherConditionType.THUNDERSTORM -> WeatherIconType.Rain.toDrawableRes()
        WeatherConditionType.SNOW -> WeatherIconType.Snow.toDrawableRes()
        WeatherConditionType.FOG -> WeatherIconType.Humidity.toDrawableRes()
    }
}
