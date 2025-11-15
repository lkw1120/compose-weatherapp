package app.kwlee.weatherapp.core.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import app.kwlee.weatherapp.core.domain.model.LocationPermissionMissingException
import app.kwlee.weatherapp.core.domain.model.LocationProviderDisabledException


@Singleton
class GpsTracker @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    @SuppressLint("MissingPermission")
    fun observeLocation(
        minTimeMillis: Long = 5_000L,
        minDistanceMeters: Float = 10f,
    ): Flow<Location> = callbackFlow {
        if (!isProviderEnabled()) {
            close(LocationProviderDisabledException())
            return@callbackFlow
        }

        if (!hasLocationPermission()) {
            close(LocationPermissionMissingException())
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location).isSuccess
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )

        providers.filter { locationManager.isProviderEnabled(it) }
            .forEach { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    minTimeMillis,
                    minDistanceMeters,
                    listener,
                    Looper.getMainLooper(),
                )
            }

        val lastKnown = providers.firstNotNullOfOrNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        lastKnown?.let { trySend(it).isSuccess }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }.flowOn(Dispatchers.IO)


    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }


    private fun isProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

