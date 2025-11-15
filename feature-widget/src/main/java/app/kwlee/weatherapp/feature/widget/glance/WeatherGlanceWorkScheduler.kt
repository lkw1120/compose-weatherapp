package app.kwlee.weatherapp.feature.widget.glance

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

object WeatherGlanceWorkScheduler {
    private const val WORK_NAME = "weather_glance_update"
    private val INTERVAL: Duration = Duration.ofHours(1)

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = PeriodicWorkRequestBuilder<WeatherGlanceUpdateWorker>(INTERVAL)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        immediateUpdateScope.launch {
            WeatherGlanceAppWidget().updateAll(context.applicationContext)
        }
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

private val immediateUpdateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@HiltWorker
class WeatherGlanceUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            WeatherGlanceAppWidget().updateAll(applicationContext)
            Result.success()
        } catch (throwable: Throwable) {
            Timber.tag("WeatherGlance").e(throwable, "Failed to update glance widget")
            Result.retry()
        }
    }
}
