package app.kwlee.weatherapp.core.domain.repository

import app.kwlee.weatherapp.core.domain.model.WeatherSettings
import kotlinx.coroutines.flow.Flow


interface SettingsRepository {
    fun observeSettings(): Flow<WeatherSettings>
    suspend fun updateSettings(settings: WeatherSettings): Result<Unit>
    suspend fun restoreDefaults(): Result<WeatherSettings>
}

