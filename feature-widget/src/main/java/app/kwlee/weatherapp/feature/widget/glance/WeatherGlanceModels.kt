package app.kwlee.weatherapp.feature.widget.glance

import androidx.annotation.DrawableRes

sealed interface WeatherGlanceUiState {
    data object Loading : WeatherGlanceUiState

    data class Success(
        val locationLabel: String,
        val temperatureLabel: String,
        val conditionLabel: String,
        val temperatureRangeLabel: String,
        val dailyItems: List<WeatherGlanceDailyItem>,
        val statusMessage: String?,
        @param:DrawableRes val iconRes: Int,
    ) : WeatherGlanceUiState

    data class Error(
        val message: String,
        val debugLabel: String? = null,
    ) : WeatherGlanceUiState
}

data class WeatherGlanceDailyItem(
    val dayLabel: String,
    val temperatureRangeLabel: String,
    @param:DrawableRes val iconRes: Int,
)
