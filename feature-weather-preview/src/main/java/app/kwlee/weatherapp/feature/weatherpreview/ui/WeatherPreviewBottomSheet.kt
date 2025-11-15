package app.kwlee.weatherapp.feature.weatherpreview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.ui.components.DailyForecastSection
import app.kwlee.weatherapp.core.ui.components.HourlyForecastSection
import app.kwlee.weatherapp.core.ui.components.WeatherDetailSection
import app.kwlee.weatherapp.core.ui.components.WeatherSummarySection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherPreviewBottomSheet(
    uiState: WeatherPreviewUiState,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    onApplyLocation: ((FavoriteLocation) -> Unit)? = null,
) {
    if (!uiState.shouldShowSheet) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {

        },
    ) {
        WeatherPreviewContent(
            uiState = uiState,
            onApplyLocation = onApplyLocation,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherPreviewContent(
    uiState: WeatherPreviewUiState,
    onApplyLocation: ((FavoriteLocation) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    //.fillMaxWidth()
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp,16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.errorMessage
                        ?: stringResource(id = app.kwlee.weatherapp.core.ui.R.string.weather_preview_error_fetch),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        uiState.weather != null -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = {
                        Text(
                            modifier = Modifier,
                            text = uiState.weather.locationLabel,
                        )
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WeatherSummarySection(
                        temperatureLabel = uiState.weather.temperatureLabel,
                        conditionLabel = uiState.weather.conditionLabel,
                        highlights = uiState.weather.summaryHighlights,
                        icon = uiState.weather.summaryIcon,
                        modifier = Modifier.padding(top = 24.dp),
                    )

                    HourlyForecastSection(
                        forecast = uiState.weather.hourlyForecast,
                        modifier = Modifier.padding(top = 32.dp),
                    )

                    DailyForecastSection(
                        forecasts = uiState.weather.dailyForecast,
                        modifier = Modifier.padding(top = 32.dp),
                    )

                    WeatherDetailSection(
                        highlights = uiState.weather.detailHighlights,
                        modifier = Modifier.padding(top = 32.dp),
                    )
                }
            }
        }
    }
}

