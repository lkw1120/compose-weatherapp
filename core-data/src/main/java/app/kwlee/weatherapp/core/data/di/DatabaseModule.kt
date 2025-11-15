package app.kwlee.weatherapp.core.data.di

import android.content.Context
import androidx.room.Room
import app.kwlee.weatherapp.core.data.local.WeatherDatabase
import app.kwlee.weatherapp.core.data.local.dao.FavoriteLocationDao
import app.kwlee.weatherapp.core.data.local.dao.WeatherOverviewCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


private const val WEATHER_DB_NAME = "weather.db"


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context,
    ): WeatherDatabase = Room.databaseBuilder(
        context,
        WeatherDatabase::class.java,
        WEATHER_DB_NAME,
    ).fallbackToDestructiveMigration(false).build()

    @Provides
    fun provideFavoriteLocationDao(weatherDatabase: WeatherDatabase): FavoriteLocationDao =
        weatherDatabase.favoriteLocationDao()

    @Provides
    fun provideWeatherOverviewCacheDao(
        weatherDatabase: WeatherDatabase,
    ): WeatherOverviewCacheDao = weatherDatabase.weatherOverviewCacheDao()
}

