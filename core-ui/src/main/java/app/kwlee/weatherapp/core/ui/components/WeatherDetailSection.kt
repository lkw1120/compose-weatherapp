package app.kwlee.weatherapp.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import app.kwlee.weatherapp.core.ui.R

@Composable
fun WeatherDetailSection(
    highlights: List<WeatherHighlightUiModel>,
    modifier: Modifier = Modifier,
) {
    if (highlights.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.weather_details_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )

        val rows = highlights.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { highlight ->
                        WeatherDetailCard(
                            modifier = Modifier.weight(1f),
                            highlight = highlight,
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailCard(
    highlight: WeatherHighlightUiModel,
    modifier: Modifier = Modifier,
) {
    val valueLines = remember(highlight.value) {
        highlight.value.split("•").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = highlight.icon.toImageVector(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )

                Text(
                    text = getHighlightTitle(highlight.type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                valueLines.forEachIndexed { index, line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 16.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
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
