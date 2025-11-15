package app.kwlee.weatherapp.feature.widget.glance

import app.kwlee.weatherapp.core.data.local.WeatherGlanceLocationStorage
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import app.kwlee.weatherapp.core.domain.usecase.GetCurrentLocationUseCase
import app.kwlee.weatherapp.core.domain.usecase.GetWeatherOverviewUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeatherGlanceEntryPoint {
    fun getWeatherOverviewUseCase(): GetWeatherOverviewUseCase
    fun settingsRepository(): SettingsRepository
    fun weatherGlanceMapper(): WeatherGlanceMapper
    fun getCurrentLocationUseCase(): GetCurrentLocationUseCase
    fun weatherGlanceLocationStorage(): WeatherGlanceLocationStorage
}
