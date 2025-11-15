package app.kwlee.weatherapp.core.service

import android.Manifest
import android.content.Context
import app.kwlee.weatherapp.core.common.AppLogger
import app.kwlee.weatherapp.core.ui.R
import com.gun0912.tedpermission.coroutine.TedPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PermissionManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    suspend fun ensureLocationPermission(): Boolean {
        val result = TedPermission.create()
            .setDeniedMessage(context.getString(R.string.permission_rationale_message))
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            .check()

        if (!result.isGranted) {
            AppLogger.d("Location permission denied: ${result.deniedPermissions.joinToString()}")
        }

        return result.isGranted
    }
}
