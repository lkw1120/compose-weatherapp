package app.kwlee.weatherapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.kwlee.weatherapp.core.data.local.dao.FavoriteLocationDao
import app.kwlee.weatherapp.core.data.local.dao.WeatherOverviewCacheDao
import app.kwlee.weatherapp.core.data.local.entity.FavoriteLocationEntity
import app.kwlee.weatherapp.core.data.local.entity.WeatherOverviewCacheEntity


@Database(
    entities = [
        FavoriteLocationEntity::class,
        WeatherOverviewCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun weatherOverviewCacheDao(): WeatherOverviewCacheDao
}

