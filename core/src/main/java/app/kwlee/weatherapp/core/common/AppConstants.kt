package app.kwlee.weatherapp.core.common

object AppConstants {
    object Network {
        const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
        const val OPEN_WEATHER_ONE_CALL_PATH = "data/3.0/onecall"
        const val OPEN_WEATHER_GEOCODING_PATH = "geo/1.0/direct"
        const val OPEN_WEATHER_REVERSE_GEOCODING_PATH = "geo/1.0/reverse"
    }

    object Widget {
        const val MIN_UPDATE_INTERVAL_MS = 5 * 60 * 1000L
        const val WORK_NAME_PREFIX = "weather_widget_refresh_"
        const val MAX_DAILY_ITEMS = 5
    }

    object Location {
        const val TIMEOUT_MS = 5_000L
    }

    object Cache {
        const val WEATHER_CACHE_EXPIRATION_MINUTES = 30L
    }
}
