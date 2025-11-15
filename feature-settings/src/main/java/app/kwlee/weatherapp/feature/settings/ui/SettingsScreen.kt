package app.kwlee.weatherapp.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.domain.model.DistanceUnit
import app.kwlee.weatherapp.core.domain.model.PrecipitationUnit
import app.kwlee.weatherapp.core.domain.model.PressureUnit
import app.kwlee.weatherapp.core.domain.model.TemperatureUnit
import app.kwlee.weatherapp.core.domain.model.TimeFormat
import app.kwlee.weatherapp.core.domain.model.WindSpeedUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.ShowSnackbar -> {
                    val messageResId = when (event.message) {
                        SettingsSnackbarMessage.RestoreDefaultsSuccess -> R.string.settings_restore_defaults_success
                        SettingsSnackbarMessage.ClearCacheSuccess -> R.string.settings_clear_cache_success
                    }
                    val message = context.getString(messageResId)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title)) },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(top = 0.dp),
    ) { paddingValues ->
        SettingsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            uiState = uiState,
            onTemperatureUnitSelected = viewModel::onTemperatureUnitSelected,
            onWindSpeedUnitSelected = viewModel::onWindSpeedUnitSelected,
            onPrecipitationUnitSelected = viewModel::onPrecipitationUnitSelected,
            onDistanceUnitSelected = viewModel::onDistanceUnitSelected,
            onPressureUnitSelected = viewModel::onPressureUnitSelected,
            onTimeFormatSelected = viewModel::onTimeFormatSelected,
            onRestoreDefaults = viewModel::onRestoreDefaults,
            onClearCache = viewModel::onClearCache,
        )
    }
}


@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onTemperatureUnitSelected: (TemperatureUnit) -> Unit,
    onWindSpeedUnitSelected: (WindSpeedUnit) -> Unit,
    onPrecipitationUnitSelected: (PrecipitationUnit) -> Unit,
    onDistanceUnitSelected: (DistanceUnit) -> Unit,
    onPressureUnitSelected: (PressureUnit) -> Unit,
    onTimeFormatSelected: (TimeFormat) -> Unit,
    onRestoreDefaults: () -> Unit,
    onClearCache: () -> Unit,
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.errorMessage != null) {
            val errorText = uiState.errorMessage.takeIf { it.isNotBlank() }
                ?: stringResource(id = R.string.settings_error_generic)
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        SettingsSectionTitle(title = stringResource(id = R.string.settings_section_app_info))
        SettingsAppName()
        SettingsAppVersion()
        SettingsDataSource()

        SettingsSectionTitle(title = stringResource(id = R.string.settings_section_display))
        SettingsToggle(
            title = stringResource(id = R.string.settings_temperature_unit),
            checked = uiState.settings.temperatureUnit == TemperatureUnit.FAHRENHEIT,
            onToggle = { isEnabled ->
                val newUnit = if (isEnabled) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
                onTemperatureUnitSelected(newUnit)
            },
            enabled = !uiState.isSaving,
        )

        SettingsToggle(
            title = stringResource(id = R.string.settings_time_format),
            checked = uiState.settings.timeFormat == TimeFormat.TWENTY_FOUR_HOUR,
            onToggle = { isEnabled ->
                val newFormat = if (isEnabled) TimeFormat.TWENTY_FOUR_HOUR else TimeFormat.TWELVE_HOUR
                onTimeFormatSelected(newFormat)
            },
            enabled = !uiState.isSaving,
        )

        SettingsSectionTitle(title = stringResource(id = R.string.settings_section_units))
        SettingsSection(
            title = stringResource(id = R.string.settings_wind_unit),
            options = listOf(
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_wind_kilometers_per_hour),
                    value = WindSpeedUnit.KILOMETERS_PER_HOUR,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_wind_miles_per_hour),
                    value = WindSpeedUnit.MILES_PER_HOUR,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_wind_meters_per_second),
                    value = WindSpeedUnit.METERS_PER_SECOND,
                ),
            ),
            selected = uiState.settings.windSpeedUnit,
            onOptionSelected = onWindSpeedUnitSelected,
            enabled = !uiState.isSaving,
        )

        SettingsSection(
            title = stringResource(id = R.string.settings_precipitation_unit),
            options = listOf(
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_precipitation_millimeters),
                    value = PrecipitationUnit.MILLIMETERS,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_precipitation_centimeters),
                    value = PrecipitationUnit.CENTIMETERS,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_precipitation_inches),
                    value = PrecipitationUnit.INCHES,
                ),
            ),
            selected = uiState.settings.precipitationUnit,
            onOptionSelected = onPrecipitationUnitSelected,
            enabled = !uiState.isSaving,
        )

        SettingsSection(
            title = stringResource(id = R.string.settings_distance_unit),
            options = listOf(
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_distance_kilometers),
                    value = DistanceUnit.KILOMETERS,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_distance_miles),
                    value = DistanceUnit.MILES,
                ),
            ),
            selected = uiState.settings.distanceUnit,
            onOptionSelected = onDistanceUnitSelected,
            enabled = !uiState.isSaving,
        )

        SettingsSection(
            title = stringResource(id = R.string.settings_pressure_unit),
            options = listOf(
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_pressure_hectopascal),
                    value = PressureUnit.HECTOPASCAL,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_pressure_millimeters_of_mercury),
                    value = PressureUnit.MILLIMETERS_OF_MERCURY,
                ),
                SettingsDropdownOption(
                    label = stringResource(id = R.string.settings_pressure_inches_of_mercury),
                    value = PressureUnit.INCHES_OF_MERCURY,
                ),
            ),
            selected = uiState.settings.pressureUnit,
            onOptionSelected = onPressureUnitSelected,
            enabled = !uiState.isSaving,
        )

        SettingsSectionTitle(title = stringResource(id = R.string.settings_section_other))
        SettingsRestoreDefaults(
            enabled = !uiState.isSaving,
            onRestoreDefaults = onRestoreDefaults,
        )
        SettingsClearCache(
            enabled = !uiState.isSaving,
            onClearCache = onClearCache,
        )
        Spacer(modifier = Modifier.height(64.dp))
    }
}


private data class SettingsDropdownOption<T>(
    val label: String,
    val value: T,
)


@Composable
private fun SettingsSectionTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier,
        )
    }
}


@Composable
private fun SettingsAppName() {
    val appName = stringResource(id = R.string.app_name)
    
    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.settings_item_app_name)) },
        trailingContent = {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp),
    )
}


@Composable
private fun SettingsAppVersion() {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName as String
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }
    
    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.settings_item_version_name)) },
        trailingContent = {
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp),
    )
}


@Composable
private fun SettingsDataSource() {
    val context = LocalContext.current
    val dataSourceUrl = stringResource(id = R.string.settings_data_source_url)

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.settings_item_data_source)) },

        trailingContent = {
            Text(
                text = stringResource(id = R.string.settings_data_source_value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dataSourceUrl))
                context.startActivity(intent)
            },
    )
}


@Composable
private fun <T> SettingsSection(
    title: String,
    options: List<SettingsDropdownOption<T>>,
    selected: T,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean,
) {
    SettingsDropdown(
        title = title,
        options = options,
        selected = selected,
        onOptionSelected = onOptionSelected,
        enabled = enabled,
    )
}


@Composable
private fun <T> SettingsDropdown(
    title: String,
    options: List<SettingsDropdownOption<T>>,
    selected: T,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selected }?.label.orEmpty()

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ListItem(
            headlineContent = { Text(text = title) },
            trailingContent = {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = selectedLabel.ifBlank { options.firstOrNull()?.label.orEmpty() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option.label) },
                                onClick = {
                                    expanded = false
                                    if (option.value != selected) {
                                        onOptionSelected(option.value)
                                    }
                                },
                                enabled = enabled,
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable(enabled = enabled) { expanded = true },
        )
    }
}


@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean,
) {
    ListItem(
        headlineContent = { Text(text = title) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onToggle(it) },
                enabled = enabled,
            )
        },
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp),
    )
}


@Composable
private fun SettingsRestoreDefaults(
    enabled: Boolean,
    onRestoreDefaults: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.settings_restore_defaults)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(enabled = enabled) {
                onRestoreDefaults()
            },
    )
}


@Composable
private fun SettingsClearCache(
    enabled: Boolean,
    onClearCache: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.settings_clear_cache)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(enabled = enabled) {
                onClearCache()
            },
    )
}
