package app.kwlee.weatherapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.kwlee.weatherapp.core.domain.model.DistanceUnit
import app.kwlee.weatherapp.core.domain.model.PrecipitationUnit
import app.kwlee.weatherapp.core.domain.model.PressureUnit
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.TimeFormat
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import app.kwlee.weatherapp.core.di.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override fun observeSettings(): Flow<WeatherSettings> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences.toWeatherSettings() }
    }

    override suspend fun updateSettings(settings: WeatherSettings): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            dataStore.edit { preferences ->
                preferences[Keys.TEMPERATURE_UNIT] = settings.temperatureUnit.name
                preferences[Keys.WIND_SPEED_UNIT] = settings.windSpeedUnit.name
                preferences[Keys.PRECIPITATION_UNIT] = settings.precipitationUnit.name
                preferences[Keys.DISTANCE_UNIT] = settings.distanceUnit.name
                preferences[Keys.PRESSURE_UNIT] = settings.pressureUnit.name
                preferences[Keys.TIME_FORMAT] = settings.timeFormat.name
            }
            Unit
        }
    }

    override suspend fun restoreDefaults(): Result<WeatherSettings> = withContext(ioDispatcher) {
        runCatching {
            val defaults = WeatherSettings()
            dataStore.edit { preferences ->
                preferences[Keys.TEMPERATURE_UNIT] = defaults.temperatureUnit.name
                preferences[Keys.WIND_SPEED_UNIT] = defaults.windSpeedUnit.name
                preferences[Keys.PRECIPITATION_UNIT] = defaults.precipitationUnit.name
                preferences[Keys.DISTANCE_UNIT] = defaults.distanceUnit.name
                preferences[Keys.PRESSURE_UNIT] = defaults.pressureUnit.name
                preferences[Keys.TIME_FORMAT] = defaults.timeFormat.name
            }
            defaults
        }
    }

    private fun Preferences.toWeatherSettings(): WeatherSettings = WeatherSettings(
        temperatureUnit = this[Keys.TEMPERATURE_UNIT]?.toEnumOrDefault(TemperatureUnit.CELSIUS) ?: TemperatureUnit.CELSIUS,
        windSpeedUnit = this[Keys.WIND_SPEED_UNIT]?.toEnumOrDefault(WindSpeedUnit.METERS_PER_SECOND) ?: WindSpeedUnit.METERS_PER_SECOND,
        precipitationUnit = this[Keys.PRECIPITATION_UNIT]?.toEnumOrDefault(PrecipitationUnit.MILLIMETERS) ?: PrecipitationUnit.MILLIMETERS,
        distanceUnit = this[Keys.DISTANCE_UNIT]?.toEnumOrDefault(DistanceUnit.KILOMETERS) ?: DistanceUnit.KILOMETERS,
        pressureUnit = this[Keys.PRESSURE_UNIT]?.toEnumOrDefault(PressureUnit.HECTOPASCAL) ?: PressureUnit.HECTOPASCAL,
        timeFormat = this[Keys.TIME_FORMAT]?.toEnumOrDefault(TimeFormat.TWELVE_HOUR) ?: TimeFormat.TWELVE_HOUR,
    )

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("settings_temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("settings_wind_speed_unit")
        val PRECIPITATION_UNIT = stringPreferencesKey("settings_precipitation_unit")
        val DISTANCE_UNIT = stringPreferencesKey("settings_distance_unit")
        val PRESSURE_UNIT = stringPreferencesKey("settings_pressure_unit")
        val TIME_FORMAT = stringPreferencesKey("settings_time_format")
    }

    private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
        return runCatching { enumValueOf<T>(this) }.getOrElse { default }
    }
}

