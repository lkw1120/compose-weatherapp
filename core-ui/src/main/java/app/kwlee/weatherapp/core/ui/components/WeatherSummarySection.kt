package app.kwlee.weatherapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.ui.components.HighlightUiType


@Composable
fun WeatherSummarySection(
    temperatureLabel: String,
    conditionLabel: String,
    highlights: List<WeatherHighlightUiModel>,
    icon: WeatherIconType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Icon(
            imageVector = icon.toImageVector(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        )


        Text(
            text = temperatureLabel,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
        )


        Text(
            text = conditionLabel,
            style = MaterialTheme.typography.titleMedium,
        )

        if (highlights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                highlights.take(3).forEach { highlight ->
                    WeatherHighlightCard(
                        highlight = highlight,
                        modifier = Modifier.weight(1f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}


@Composable
private fun WeatherHighlightCard(
    modifier: Modifier = Modifier,
    highlight: WeatherHighlightUiModel,
    contentColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(contentColor.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = highlight.icon.toImageVector(),
                contentDescription = null,
                tint = contentColor,
            )
        }

        Text(
            text = getHighlightTitle(highlight.type),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Text(
            text = highlight.value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun getHighlightTitle(type: HighlightUiType): String {
    return when (type) {
        HighlightUiType.FEELS_LIKE -> stringResource(R.string.highlight_feels_like)
        HighlightUiType.HUMIDITY -> stringResource(R.string.highlight_humidity)
        HighlightUiType.PRECIPITATION -> stringResource(R.string.highlight_precipitation)
        HighlightUiType.PRECIPITATION_PROBABILITY -> stringResource(R.string.highlight_precipitation_probability)
        HighlightUiType.SNOW -> stringResource(R.string.highlight_snow)
        HighlightUiType.WIND -> stringResource(R.string.highlight_wind)
        HighlightUiType.WIND_DIRECTION -> stringResource(R.string.highlight_wind_direction)
        HighlightUiType.UV_INDEX -> stringResource(R.string.highlight_uv_index)
        HighlightUiType.PRESSURE -> stringResource(R.string.highlight_pressure)
        HighlightUiType.CLOUDINESS -> stringResource(R.string.highlight_cloudiness)
        HighlightUiType.VISIBILITY -> stringResource(R.string.highlight_visibility)
        HighlightUiType.SUNRISE -> stringResource(R.string.highlight_sunrise)
        HighlightUiType.SUNSET -> stringResource(R.string.highlight_sunset)
        HighlightUiType.ALERT -> stringResource(R.string.highlight_alert)
        HighlightUiType.AIR_QUALITY -> stringResource(R.string.highlight_air_quality)
    }
}

