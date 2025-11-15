package app.kwlee.weatherapp.di

import app.kwlee.weatherapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {
    @Provides
    @Singleton
    @Named("OpenWeatherApiKey")
    fun provideOpenWeatherApiKey(): String = BuildConfig.OPEN_WEATHER_API_KEY
}

