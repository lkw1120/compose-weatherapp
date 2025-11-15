package app.kwlee.weatherapp.feature.widget.glance

import android.content.Context
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.domain.model.LocationResult
import app.kwlee.weatherapp.feature.widget.glance.WeatherGlanceMapper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class WeatherGlanceContentBuilder(private val context: Context) {

    private val entryPoint: WeatherGlanceEntryPoint by lazy {
        EntryPointAccessors.fromApplication(context, WeatherGlanceEntryPoint::class.java)
    }

    suspend fun build(): Result<WeatherGlanceUiState.Success> {
        Timber.tag(TAG).d("observeSettings() start")
        val settingsRepository = entryPoint.settingsRepository()
        val settings = settingsRepository.observeSettings().firstOrNull().also {
            Timber.tag(TAG).d("observeSettings() done -> %s", it)
        } ?: return Result.failure(BuildException.SettingsUnavailable)

        Timber.tag(TAG).d("getCurrentLocationUseCase() start")
        val locationOutcome = entryPoint.getCurrentLocationUseCase()()
        val storage = entryPoint.weatherGlanceLocationStorage()
        val (location, statusMessage) = when (locationOutcome) {
            is LocationResult.Success -> locationOutcome.location.also {
                Timber.tag(TAG).d("location success lat=%f lon=%f", it.latitude, it.longitude)
                storage.save(it)
            } to null
            LocationResult.PermissionRequired -> {
                Timber.tag(TAG).w("location permission required")
                return Result.failure(BuildException.LocationPermission)
            }
            LocationResult.ProviderDisabled -> {
                Timber.tag(TAG).w("location provider disabled")
                return Result.failure(BuildException.LocationProvider)
            }
            is LocationResult.Error -> {
                Timber.tag(TAG).e(locationOutcome.throwable, "location error")
                val fallbackLocation = storage.get()
                if (fallbackLocation != null) {
                    Timber.tag(TAG).w(
                        "using cached location lat=%f lon=%f",
                        fallbackLocation.latitude,
                        fallbackLocation.longitude,
                    )
                    fallbackLocation to context.getString(R.string.widget_status_cached_location)
                } else {
                    return Result.failure(BuildException.LocationError(locationOutcome.throwable))
                }
            }
        }

        Timber.tag(TAG).d("getWeatherOverviewUseCase() start")
        val overviewResult = entryPoint.getWeatherOverviewUseCase()(location)
        val overview = overviewResult.getOrElse { throwable ->
            Timber.tag(TAG).e(throwable, "Failed to fetch weather overview for widget")
            return Result.failure(BuildException.OverviewError(throwable))
        }
        Timber.tag(TAG).d(
            "overview fetched -> location=%s daily=%d hourly=%d",
            overview.locationName,
            overview.daily.size,
            overview.hourly.size,
        )

        val glanceMapper = entryPoint.weatherGlanceMapper()
        val glanceModel = glanceMapper.map(overview, settings, context, MAX_DAILY_ITEMS)

        val result = Result.success(
            WeatherGlanceUiState.Success(
                locationLabel = glanceModel.locationLabel,
                temperatureLabel = glanceModel.temperatureLabel,
                conditionLabel = glanceModel.conditionLabel,
                temperatureRangeLabel = glanceModel.temperatureRangeLabel,
                dailyItems = glanceModel.dailyItems,
                statusMessage = statusMessage,
                iconRes = glanceModel.iconRes,
            ),
        )
        Timber.tag(TAG).d("build() success -> %s", result)
        return result
    }

    companion object {
        private const val TAG = "WeatherGlanceBuilder"
        private const val MAX_DAILY_ITEMS = 7
    }

    sealed class BuildException(
        val stage: Stage,
        cause: Throwable? = null,
    ) : Exception(cause) {
        enum class Stage {
            SETTINGS,
            LOCATION,
            OVERVIEW,
            UNKNOWN,
        }

        data object SettingsUnavailable : BuildException(Stage.SETTINGS)

        data object LocationPermission : BuildException(Stage.LOCATION)

        data object LocationProvider : BuildException(Stage.LOCATION)

        class LocationError(cause: Throwable) : BuildException(Stage.LOCATION, cause)

        class OverviewError(cause: Throwable) : BuildException(Stage.OVERVIEW, cause)
    }
}
