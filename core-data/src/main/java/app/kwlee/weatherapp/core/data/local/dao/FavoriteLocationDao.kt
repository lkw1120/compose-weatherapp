package app.kwlee.weatherapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.kwlee.weatherapp.core.data.local.entity.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FavoriteLocationDao {

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun observeFavoriteLocations(): Flow<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(location: FavoriteLocationEntity)

    @Delete
    suspend fun delete(location: FavoriteLocationEntity)

    @Query("SELECT * FROM favorite_locations WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun findByCoordinates(latitude: Double, longitude: Double): FavoriteLocationEntity?
}

