package app.kwlee.weatherapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import app.kwlee.weatherapp.core.ui.R
import com.composables.icons.lucide.Cloud
import com.composables.icons.lucide.CloudMoon
import com.composables.icons.lucide.CloudRain
import com.composables.icons.lucide.CloudSun
import com.composables.icons.lucide.Cloudy
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.composables.icons.lucide.Thermometer
import com.composables.icons.lucide.Wind


enum class WeatherIconType {
    Sun,
    Moon,
    Cloud,
    CloudSun,
    CloudMoon,
    Cloudy,
    Rain,
    Snow,
    Humidity,
    Thermometer,
    Wind,
    Gauge,
    Compass,
    Eye,
    Sunrise,
    Sunset,
    Alert,
}


@Composable
fun WeatherIconType.toImageVector(): ImageVector = when (this) {
    WeatherIconType.Sun -> Lucide.Sun
    WeatherIconType.Moon -> Lucide.MoonStar
    WeatherIconType.Cloud -> Lucide.Cloud
    WeatherIconType.CloudSun -> Lucide.CloudSun
    WeatherIconType.CloudMoon -> Lucide.CloudMoon
    WeatherIconType.Cloudy -> Lucide.Cloudy
    WeatherIconType.Rain -> Lucide.CloudRain
    WeatherIconType.Snow -> ImageVector.vectorResource(R.drawable.ic_snowflake)
    WeatherIconType.Humidity -> Lucide.Droplets
    WeatherIconType.Thermometer -> Lucide.Thermometer
    WeatherIconType.Wind -> Lucide.Wind
    WeatherIconType.Gauge -> Lucide.Gauge
    WeatherIconType.Compass -> Lucide.Compass
    WeatherIconType.Eye -> Lucide.Eye
    WeatherIconType.Sunrise -> Lucide.Sunrise
    WeatherIconType.Sunset -> Lucide.Sunset
    WeatherIconType.Alert -> ImageVector.vectorResource(R.drawable.ic_cloud_alert)
}


