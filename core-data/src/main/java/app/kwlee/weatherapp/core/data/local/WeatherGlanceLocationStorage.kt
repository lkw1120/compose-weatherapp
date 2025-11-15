package app.kwlee.weatherapp.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import app.kwlee.weatherapp.core.di.IoDispatcher
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.PreferenceDataStoreFactory

@Singleton
class WeatherGlanceLocationStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope,
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(STORE_FILE_NAME) },
    )

    suspend fun save(location: FavoriteLocation) = runCatching {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[LATITUDE_KEY] = location.latitude
                prefs[LONGITUDE_KEY] = location.longitude
            }
        }
    }.getOrElse { throwable ->
        if (throwable is CancellationException) throw throwable
    }

    suspend fun get(): FavoriteLocation? = withContext(ioDispatcher) {
        val prefs = runCatching { dataStore.data.firstOrNull() }.getOrNull() ?: return@withContext null
        val latitude = prefs[LATITUDE_KEY] ?: return@withContext null
        val longitude = prefs[LONGITUDE_KEY] ?: return@withContext null
        FavoriteLocation(
            name = "",
            latitude = latitude,
            longitude = longitude,
            isPrimary = true,
        )
    }

    suspend fun clear() = runCatching {
        withContext(ioDispatcher) {
            dataStore.edit { it.clear() }
        }
    }.getOrElse { throwable ->
        if (throwable is CancellationException) throw throwable
    }

    companion object {
        private const val STORE_FILE_NAME = "weather_glance_location.preferences_pb"
        private val LATITUDE_KEY = doublePreferencesKey("latitude")
        private val LONGITUDE_KEY = doublePreferencesKey("longitude")
    }
}

