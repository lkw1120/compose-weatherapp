package app.kwlee.weatherapp.core.data.di

import app.kwlee.weatherapp.core.data.repository.FavoriteRepositoryImpl
import app.kwlee.weatherapp.core.data.repository.LocationRepositoryImpl
import app.kwlee.weatherapp.core.data.repository.SearchHistoryRepositoryImpl
import app.kwlee.weatherapp.core.data.repository.SettingsRepositoryImpl
import app.kwlee.weatherapp.core.data.repository.WeatherRepositoryImpl
import app.kwlee.weatherapp.core.domain.repository.FavoriteRepository
import app.kwlee.weatherapp.core.domain.repository.LocationRepository
import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import app.kwlee.weatherapp.core.domain.repository.SettingsRepository
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository
}

