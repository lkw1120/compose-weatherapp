package app.kwlee.weatherapp.core.domain.model


data class WeatherSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MILLIMETERS,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val pressureUnit: PressureUnit = PressureUnit.HECTOPASCAL,
    val timeFormat: TimeFormat = TimeFormat.TWELVE_HOUR,
)


enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
}


enum class WindSpeedUnit {
    KILOMETERS_PER_HOUR,
    MILES_PER_HOUR,
    METERS_PER_SECOND,
}


enum class PrecipitationUnit {
    MILLIMETERS,
    CENTIMETERS,
    INCHES,
}


enum class DistanceUnit {
    KILOMETERS,
    MILES,
}


enum class TimeFormat {
    TWENTY_FOUR_HOUR,
    TWELVE_HOUR,
}


enum class PressureUnit {
    HECTOPASCAL,
    MILLIMETERS_OF_MERCURY,
    INCHES_OF_MERCURY,
}

