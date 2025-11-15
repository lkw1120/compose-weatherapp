package app.kwlee.weatherapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.service.PermissionManager
import app.kwlee.weatherapp.feature.main.ui.MainPermissionEvent
import app.kwlee.weatherapp.navigation.WeatherAppNavHost
import app.kwlee.weatherapp.core.theme.WeatherAppTheme
import app.kwlee.weatherapp.feature.widget.glance.WeatherGlanceWorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private val permissionEvents =
        MutableSharedFlow<MainPermissionEvent>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocationPermission()
        setContent {
            WeatherAppTheme {
                WeatherAppNavHost(permissionEvents = permissionEvents.asSharedFlow())
            }
        }
    }

    private fun requestLocationPermission() {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                owner.lifecycleScope.launch {
                    val granted = permissionManager.ensureLocationPermission()
                    if (granted) {
                        permissionEvents.tryEmit(MainPermissionEvent.Granted)
                        WeatherGlanceWorkScheduler.schedule(applicationContext)
                    } else {
                        permissionEvents.tryEmit(MainPermissionEvent.Denied)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.permission_denied_message),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        })
    }
}