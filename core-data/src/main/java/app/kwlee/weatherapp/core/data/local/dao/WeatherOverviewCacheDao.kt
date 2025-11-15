package app.kwlee.weatherapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.kwlee.weatherapp.core.data.local.entity.WeatherOverviewCacheEntity

@Dao
interface WeatherOverviewCacheDao {

    @Query(
        value = """
        SELECT * FROM weather_overview_cache 
        WHERE latitude = :latitude AND longitude = :longitude
        LIMIT 1
        """,
    )
    suspend fun findByCoordinates(latitude: Double, longitude: Double): WeatherOverviewCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherOverviewCacheEntity)

    @Query("DELETE FROM weather_overview_cache WHERE updatedAtEpochMillis < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("DELETE FROM weather_overview_cache")
    suspend fun deleteAll()
}

