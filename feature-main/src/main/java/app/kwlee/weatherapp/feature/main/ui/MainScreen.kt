package app.kwlee.weatherapp.feature.main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.ui.components.DailyForecastSection
import app.kwlee.weatherapp.core.ui.components.DailyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.HourlyForecastSection
import app.kwlee.weatherapp.core.ui.components.HourlyForecastUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherAlertCard
import app.kwlee.weatherapp.core.ui.components.WeatherDetailSection
import app.kwlee.weatherapp.core.ui.components.WeatherHighlightUiModel
import app.kwlee.weatherapp.core.ui.components.WeatherIconType
import app.kwlee.weatherapp.core.ui.components.WeatherSummarySection
import app.kwlee.weatherapp.core.ui.mapper.WeatherAlertUiModel

import app.kwlee.weatherapp.core.theme.WeatherAppTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.HtmlCompat
import app.kwlee.weatherapp.core.ui.components.HighlightUiType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    permissionEvents: Flow<MainPermissionEvent> = emptyFlow(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MainUiEvent.ShowMessage -> {
                    val messageResId = event.errorType.toResourceId()
                    val message = if (event.formatArgs.isEmpty()) {
                        HtmlCompat.fromHtml(
                            context.getString(messageResId),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                    } else {
                        HtmlCompat.fromHtml(
                            context.getString(messageResId, *event.formatArgs.toTypedArray()),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    LaunchedEffect(permissionEvents, viewModel) {
        permissionEvents.collectLatest { event ->
            when (event) {
                MainPermissionEvent.Granted -> viewModel.onLocationPermissionGranted()
                MainPermissionEvent.Denied -> viewModel.onLocationPermissionDenied()
            }
        }
    }

    MainScreenContent(
        modifier = modifier,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.refreshCurrentWeather() },
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun MainScreenContent(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = onRefresh,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when {
                        uiState.hasWeatherData && uiState.locationLabel.isNotBlank() -> uiState.locationLabel
                        uiState.hasWeatherData -> stringResource(id = R.string.default_location_label)
                        else -> stringResource(id = R.string.app_name)
                    }
                    Text(title)
                },
            )
        },
        contentWindowInsets = WindowInsets(top = 0.dp),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (uiState.hasWeatherData) {
                    Spacer(modifier = Modifier.height(24.dp))

                WeatherSummarySection(
                    temperatureLabel = uiState.temperatureLabel,
                    conditionLabel = uiState.conditionLabel,
                    highlights = uiState.summaryHighlights,
                    icon = uiState.summaryIcon,
                )

                if (uiState.alerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        uiState.alerts.forEach { alert ->
                            WeatherAlertCard(
                                event = alert.event,
                                description = alert.description,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HourlyForecastSection(forecast = uiState.hourlyForecast)

                Spacer(modifier = Modifier.height(32.dp))

                DailyForecastSection(forecasts = uiState.dailyForecast)

                        Spacer(modifier = Modifier.height(32.dp))
                    WeatherDetailSection(
                        highlights = uiState.detailHighlights,
                        )

                    Spacer(modifier = Modifier.height(32.dp))
                } else if (!uiState.isLoading) {
                    Spacer(modifier = Modifier.height(48.dp))
                    MainEmptyState(errorType = uiState.statusErrorType)
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}


@Composable
private fun MainEmptyState(errorType: MainErrorType?) {
    val messageResId = errorType?.toResourceId() ?: R.string.main_empty_state_default
    val message = stringResource(id = messageResId)
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    )
}


@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    WeatherAppTheme {
        MainScreenContent(
            uiState = MainUiState(
                isLoading = false,
                locationLabel = stringResource(id = R.string.default_location_label),
                temperatureLabel = "23°C",
                conditionLabel = "맑음",
                summaryIcon = WeatherIconType.Sun,
                summaryHighlights = listOf(
                    WeatherHighlightUiModel(HighlightUiType.FEELS_LIKE, "22°C", WeatherIconType.Thermometer),
                    WeatherHighlightUiModel(HighlightUiType.HUMIDITY, "58%", WeatherIconType.Humidity),
                    WeatherHighlightUiModel(HighlightUiType.PRECIPITATION, "0mm", WeatherIconType.Rain),
                ),
                detailHighlights = listOf(
                    WeatherHighlightUiModel(HighlightUiType.WIND, "12.3 km/h", WeatherIconType.Wind),
                    WeatherHighlightUiModel(HighlightUiType.WIND_DIRECTION, "NE (45°)", WeatherIconType.Compass),
                    WeatherHighlightUiModel(HighlightUiType.PRESSURE, "1015 hPa", WeatherIconType.Gauge),
                    WeatherHighlightUiModel(HighlightUiType.UV_INDEX, "4.5 (보통)", WeatherIconType.Sun),
                    WeatherHighlightUiModel(HighlightUiType.CLOUDINESS, "20%", WeatherIconType.Cloudy),
                    WeatherHighlightUiModel(HighlightUiType.VISIBILITY, "10 km", WeatherIconType.Eye),
                    WeatherHighlightUiModel(HighlightUiType.SUNRISE, "06:30", WeatherIconType.Sunrise),
                    WeatherHighlightUiModel(HighlightUiType.SUNSET, "18:45", WeatherIconType.Sunset),
                ),
                hourlyForecast = listOf(
                    HourlyForecastUiModel("09:00", WeatherIconType.Sun, "19°C"),
                    HourlyForecastUiModel("12:00", WeatherIconType.Sun, "23°C"),
                ),
                dailyForecast = listOf(
                    DailyForecastUiModel("오늘", WeatherIconType.Sun, "25°C", "15°C"),
                    DailyForecastUiModel("내일", WeatherIconType.Cloud, "24°C", "14°C"),
                ),
                alerts = emptyList(),
                isFavorite = true,
                hasWeatherData = true,
            ),
            snackbarHostState = SnackbarHostState(),
            onRefresh = {},
        )
    }
}