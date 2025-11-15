package app.kwlee.weatherapp.core.data.repository

import app.kwlee.weatherapp.core.common.AppConstants
import app.kwlee.weatherapp.core.data.local.WeatherGlanceLocationStorage
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.repository.LocationRepository
import app.kwlee.weatherapp.core.di.IoDispatcher
import app.kwlee.weatherapp.core.service.GpsTracker
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale


class LocationRepositoryImpl @Inject constructor(
    private val gpsTracker: GpsTracker,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val glanceLocationStorage: WeatherGlanceLocationStorage,
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<FavoriteLocation> = withContext(ioDispatcher) {
        runCatching {
            withTimeout(AppConstants.Location.TIMEOUT_MS) {
                val location = gpsTracker.observeLocation(
                    minTimeMillis = 0L,
                    minDistanceMeters = 0f,
                ).first()
                FavoriteLocation(
                    name = "",
                    latitude = location.latitude.roundCoordinate(),
                    longitude = location.longitude.roundCoordinate(),
                    isPrimary = true,
                ).also { favoriteLocation ->
                    glanceLocationStorage.save(favoriteLocation)
                }
            }
        }
    }
}


private fun Double.roundCoordinate(): Double {
    return String.format(Locale.US, "%.6f", this).toDouble()
}

