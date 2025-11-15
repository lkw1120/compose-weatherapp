package app.kwlee.weatherapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.kwlee.weatherapp.core.domain.model.DistanceUnit
import app.kwlee.weatherapp.core.domain.model.PrecipitationUnit
import app.kwlee.weatherapp.core.domain.model.PressureUnit
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.TimeFormat
import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class SettingsRepositoryImplTest {

    private lateinit var mockDataStore: MockDataStore
    private lateinit var repository: SettingsRepositoryImpl
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setup() {
        mockDataStore = MockDataStore()
        repository = SettingsRepositoryImpl(mockDataStore, testDispatcher)
    }

    @Test
    fun `observeSettings returns default settings when preferences empty`() = runTest(testDispatcher) {
        mockDataStore.setPreferences(emptyPreferences())

        val result = repository.observeSettings().first()

        assertEquals(WeatherSettings(), result)
    }

    @Test
    fun `observeSettings returns settings from preferences`() = runTest(testDispatcher) {
        val mutablePreferences = emptyPreferences().toMutablePreferences()
        mutablePreferences[stringPreferencesKey("settings_temperature_unit")] = TemperatureUnit.FAHRENHEIT.name
        mutablePreferences[stringPreferencesKey("settings_wind_speed_unit")] = WindSpeedUnit.MILES_PER_HOUR.name
        mockDataStore.setPreferences(mutablePreferences)

        val result = repository.observeSettings().first()

        assertEquals(TemperatureUnit.FAHRENHEIT, result.temperatureUnit)
        assertEquals(WindSpeedUnit.MILES_PER_HOUR, result.windSpeedUnit)
    }

    @Test
    fun `updateSettings saves settings to preferences`() = runTest(testDispatcher) {
        val settings = WeatherSettings(
            temperatureUnit = TemperatureUnit.FAHRENHEIT,
            windSpeedUnit = WindSpeedUnit.MILES_PER_HOUR,
        )

        val result = repository.updateSettings(settings)

        assertTrue(result.isSuccess)
        assertEquals(1, mockDataStore.editCallCount)
        val savedPreferences = mockDataStore.lastEditedPreferences
        assertEquals(TemperatureUnit.FAHRENHEIT.name, savedPreferences?.get(stringPreferencesKey("settings_temperature_unit")))
        assertEquals(WindSpeedUnit.MILES_PER_HOUR.name, savedPreferences?.get(stringPreferencesKey("settings_wind_speed_unit")))
    }

    @Test
    fun `updateSettings returns failure when dataStore throws exception`() = runTest(testDispatcher) {
        val settings = WeatherSettings()
        val exception = Exception("DataStore error")
        mockDataStore.setException(exception)

        val result = repository.updateSettings(settings)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `restoreDefaults saves default settings and returns them`() = runTest(testDispatcher) {
        val result = repository.restoreDefaults()

        assertTrue(result.isSuccess)
        assertEquals(WeatherSettings(), result.getOrNull())
        assertEquals(1, mockDataStore.editCallCount)
        val savedPreferences = mockDataStore.lastEditedPreferences
        assertEquals(TemperatureUnit.CELSIUS.name, savedPreferences?.get(stringPreferencesKey("settings_temperature_unit")))
        assertEquals(WindSpeedUnit.METERS_PER_SECOND.name, savedPreferences?.get(stringPreferencesKey("settings_wind_speed_unit")))
    }

    @Test
    fun `restoreDefaults returns failure when dataStore throws exception`() = runTest(testDispatcher) {
        val exception = Exception("DataStore error")
        mockDataStore.setException(exception)

        val result = repository.restoreDefaults()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockDataStore : DataStore<Preferences> {
        private var _preferences: Preferences = emptyPreferences()
        private var exception: Throwable? = null
        var editCallCount = 0
        var lastEditedPreferences: Preferences? = null
        private val _data = MutableStateFlow(_preferences)

        fun setPreferences(preferences: Preferences) {
            _preferences = preferences
            _data.value = preferences
        }

        fun setException(exception: Throwable) {
            this.exception = exception
        }

        override val data: Flow<Preferences> = _data

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            editCallCount++
            if (exception != null) throw exception!!
            val mutablePreferences = _preferences.toMutablePreferences()
            val updated = transform(mutablePreferences)
            lastEditedPreferences = updated
            _preferences = updated
            _data.value = updated
            return updated
        }
    }
}

