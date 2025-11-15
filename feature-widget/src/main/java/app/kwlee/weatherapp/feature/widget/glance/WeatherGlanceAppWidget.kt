package app.kwlee.weatherapp.feature.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import app.kwlee.weatherapp.core.ui.R
import java.util.Locale
import kotlin.math.abs
import timber.log.Timber

class WeatherGlanceAppWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode =
        SizeMode.Responsive(
            setOf(
                DpSize(width = 110.dp, height = 40.dp),
                DpSize(width = 180.dp, height = 40.dp),
                DpSize(width = 250.dp, height = 40.dp),
                DpSize(width = 320.dp, height = 40.dp),
                DpSize(width = 40.dp, height = 110.dp),
                DpSize(width = 110.dp, height = 110.dp),
                DpSize(width = 180.dp, height = 110.dp),
                DpSize(width = 250.dp, height = 110.dp),
                DpSize(width = 320.dp, height = 110.dp),
            ),
        )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.tag(TAG).d("provideGlance start id=%s", id)
        Timber.tag(TAG).d("build() executing")
        val buildResult = WeatherGlanceContentBuilder(context).build()

        val uiState: WeatherGlanceUiState = buildResult.getOrNull()?.also {
            Timber.tag(TAG).d("build() success -> state=%s", it::class.simpleName)
        } ?: run {
            val throwable = buildResult.exceptionOrNull()
            val errorState = (throwable ?: IllegalStateException("Unknown widget state")).toUiError(context)
            throwable?.let {
                Timber.tag(TAG).e(it, "build() failed -> %s", errorState.debugLabel)
            } ?: Timber.tag(TAG).e("build() failed without throwable -> %s", errorState.debugLabel)
            errorState
        }

        provideContent {
            GlanceTheme {
                WeatherGlanceContent(uiState = uiState)
            }
        }
        Timber.tag(TAG).d("provideGlance end id=%s state=%s", id, uiState::class.simpleName)
    }

    companion object {
        private const val TAG = "WeatherGlance"
    }
}

class WeatherGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherGlanceAppWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherGlanceWorkScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherGlanceWorkScheduler.cancel(context)
    }
}

@Composable
private fun WeatherGlanceContent(uiState: WeatherGlanceUiState) {
    val size = LocalSize.current
    Timber.tag("WeatherGlance").d("WeatherGlanceContent size=%s x %s", size.width, size.height)
    val layout = WeatherGlanceLayout.from(size)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(layout.contentPadding)
            .clickable(actionRunCallback<RefreshActionCallback>()),
        contentAlignment = layout.contentAlignment,
    ) {
        when (uiState) {
            WeatherGlanceUiState.Loading -> layout.LoadingContent()
            is WeatherGlanceUiState.Error -> layout.ErrorContent(uiState)
            is WeatherGlanceUiState.Success -> layout.SuccessContent(uiState)
        }
    }
}


private sealed class WeatherGlanceLayout(
    val contentPadding: Dp,
    val contentAlignment: Alignment,
    private val indicatorSize: Dp,
    private val errorFontSizeSp: Float,
    private val debugFontSizeSp: Float,
    private val errorMaxLines: Int = 4,
) {

    abstract fun matches(size: DpSize): Boolean

    @Composable
    open fun LoadingContent() {
        WeatherGlanceLoadingIndicator(indicatorSize)
    }

    @Composable
    open fun ErrorContent(state: WeatherGlanceUiState.Error) {
        WeatherGlanceErrorMessage(
            message = state.message,
            debugLabel = state.debugLabel,
            fontSizeSp = errorFontSizeSp,
            debugFontSizeSp = debugFontSizeSp,
            maxLines = errorMaxLines,
        )
    }

    @Composable
    abstract fun SuccessContent(state: WeatherGlanceUiState.Success)

    companion object {
        private val layouts = listOf(
            ExtraCompact,
            ShortWide,
            ShortWideExtended,
            CompactSquare,
            MediumWide,
            LargeWide,
            ExtraWide,
        )

        private val exactSizeLayouts = listOf(
            ExactLayout(size = DpSize(width = 40.dp, height = 110.dp), layout = ExtraCompact),
            ExactLayout(size = DpSize(width = 110.dp, height = 40.dp), layout = ExtraCompact),
            ExactLayout(size = DpSize(width = 180.dp, height = 40.dp), layout = ShortWide),
            ExactLayout(size = DpSize(width = 250.dp, height = 40.dp), layout = ShortWideExtended),
            ExactLayout(size = DpSize(width = 320.dp, height = 40.dp), layout = ShortWideExtended),
            ExactLayout(size = DpSize(width = 110.dp, height = 110.dp), layout = CompactSquare),
            ExactLayout(size = DpSize(width = 180.dp, height = 110.dp), layout = MediumWide),
            ExactLayout(size = DpSize(width = 250.dp, height = 110.dp), layout = LargeWide),
            ExactLayout(size = DpSize(width = 320.dp, height = 110.dp), layout = ExtraWide),
        )

        private const val SIZE_MATCH_EPSILON = 1f

        fun from(size: DpSize): WeatherGlanceLayout {
            exactSizeLayouts.firstOrNull { candidate ->
                size.isApproximately(candidate.size, SIZE_MATCH_EPSILON)
            }?.let { candidate ->
                return candidate.layout
            }
            return layouts.firstOrNull { it.matches(size) } ?: ExtraWide
        }

        private data class ExactLayout(
            val size: DpSize,
            val layout: WeatherGlanceLayout,
        )
    }

    data object ExtraCompact : WeatherGlanceLayout(
        contentPadding = 4.dp,
        contentAlignment = Alignment.Center,
        indicatorSize = 18.dp,
        errorFontSizeSp = 12f,
        debugFontSizeSp = 10f,
        errorMaxLines = 2,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height <= 72.dp && size.width <= 110.dp

        @Composable
        override fun ErrorContent(state: WeatherGlanceUiState.Error) {
            WeatherGlanceErrorMessage(
                message = state.message,
                debugLabel = null,
                fontSizeSp = 12f,
                debugFontSizeSp = 10f,
                maxLines = 2,
            )
        }

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceExtraCompactSuccess(state)
        }
    }

    data object ShortWide : WeatherGlanceLayout(
        contentPadding = 8.dp,
        contentAlignment = Alignment.CenterStart,
        indicatorSize = 20.dp,
        errorFontSizeSp = 12f,
        debugFontSizeSp = 10f,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height in 72.dp..96.dp && size.width > 110.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceShortWideSuccess(state)
        }
    }

    data object ShortWideExtended : WeatherGlanceLayout(
        contentPadding = 10.dp,
        contentAlignment = Alignment.CenterStart,
        indicatorSize = 22.dp,
        errorFontSizeSp = 12f,
        debugFontSizeSp = 10f,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height <= 72.dp && size.width >= 220.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceShortWideExtendedSuccess(state)
        }
    }

    data object CompactSquare : WeatherGlanceLayout(
        contentPadding = 10.dp,
        contentAlignment = Alignment.Center,
        indicatorSize = 22.dp,
        errorFontSizeSp = 12f,
        debugFontSizeSp = 10f,
        errorMaxLines = 3,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height > 96.dp && size.width <= 120.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceCompactSquareSuccess(state)
        }
    }

    data object MediumWide : WeatherGlanceLayout(
        contentPadding = 14.dp,
        contentAlignment = Alignment.TopStart,
        indicatorSize = 26.dp,
        errorFontSizeSp = 13f,
        debugFontSizeSp = 11f,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height > 96.dp && size.width in 180.dp..220.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceMediumWideSuccess(state)
        }
    }

    data object LargeWide : WeatherGlanceLayout(
        contentPadding = 16.dp,
        contentAlignment = Alignment.TopStart,
        indicatorSize = 30.dp,
        errorFontSizeSp = 14f,
        debugFontSizeSp = 12f,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height > 96.dp && size.width in 220.dp..260.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceLargeWideSuccess(state)
        }
    }

    data object ExtraWide : WeatherGlanceLayout(
        contentPadding = 18.dp,
        contentAlignment = Alignment.TopStart,
        indicatorSize = 32.dp,
        errorFontSizeSp = 14f,
        debugFontSizeSp = 12f,
    ) {
        override fun matches(size: DpSize): Boolean =
            size.height > 96.dp && size.width > 260.dp

        @Composable
        override fun SuccessContent(state: WeatherGlanceUiState.Success) {
            WeatherGlanceExtraWideSuccess(state)
        }
    }
}


private fun DpSize.isApproximately(other: DpSize, epsilon: Float): Boolean {
    return abs(width.value - other.width.value) <= epsilon &&
        abs(height.value - other.height.value) <= epsilon
}

@Composable
private fun WeatherGlanceExtraCompactSuccess(state: WeatherGlanceUiState.Success) {
    Text(
        text = state.temperatureLabel,
        style = TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
    )
}


@Composable
private fun WeatherGlanceShortWideSuccess(state: WeatherGlanceUiState.Success) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(state.iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(48.dp),
        )
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = state.locationLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                ),
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.temperatureLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = state.conditionLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}


@Composable
private fun WeatherGlanceShortWideExtendedSuccess(state: WeatherGlanceUiState.Success) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(state.iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(48.dp),
        )
        Column(
            modifier = GlanceModifier
                .defaultWeight(),
            horizontalAlignment = Alignment.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.locationLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                ),
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.temperatureLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = state.conditionLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}


@Composable
private fun WeatherGlanceCompactSquareSuccess(state: WeatherGlanceUiState.Success) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            provider = ImageProvider(state.iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(48.dp),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = state.temperatureLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
        Text(
            text = state.conditionLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun WeatherGlanceMediumWideSuccess(state: WeatherGlanceUiState.Success) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.locationLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Image(
            provider = ImageProvider(state.iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(56.dp),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = state.temperatureLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
        Text(
            text = state.conditionLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
        Text(
            text = state.temperatureRangeLabel,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
        state.statusMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                modifier = GlanceModifier.padding(top = 10.dp),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 2,
            )
        }
    }
}


@Composable
private fun WeatherGlanceLargeWideSuccess(state: WeatherGlanceUiState.Success) {
    WeatherGlanceDetailedSuccessContent(
        state = state,
        iconSize = 80.dp,
        horizontalAlignment = Alignment.End,
        textAlign = TextAlign.Start,
        temperatureFontSize = 30f,
        locationFontSize = 16f,
        conditionFontSize = 13f,
        rangeFontSize = 13f,
        showCondition = true,
        showRange = true,
        showDaily = true,
        dailyForecastCount = 4,
        showStatusMessage = false,
        columnSpacing = 12.dp
    )
}


@Composable
private fun WeatherGlanceExtraWideSuccess(state: WeatherGlanceUiState.Success) {
    WeatherGlanceDetailedSuccessContent(
        state = state,
        iconSize = 96.dp,
        horizontalAlignment = Alignment.End,
        textAlign = TextAlign.Start,
        temperatureFontSize = 32f,
        locationFontSize = 16f,
        conditionFontSize = 13f,
        rangeFontSize = 13f,
        showCondition = true,
        showRange = true,
        showDaily = true,
        dailyForecastCount = 4,
        showStatusMessage = true,
        columnSpacing = 12.dp
    )
}


@Composable
private fun WeatherGlanceLoadingIndicator(size: Dp) {
    CircularProgressIndicator(
        modifier = GlanceModifier.size(size),
        color = GlanceTheme.colors.onSurface,
    )
}


@Composable
private fun WeatherGlanceErrorMessage(
    message: String,
    debugLabel: String?,
    fontSizeSp: Float,
    debugFontSizeSp: Float,
    maxLines: Int,
) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = fontSizeSp.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = maxLines,
        )
        if (!debugLabel.isNullOrBlank()) {
            Text(
                text = debugLabel,
                modifier = GlanceModifier.padding(top = 2.dp),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = debugFontSizeSp.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Normal,
                ),
                maxLines = maxLines,
            )
        }
    }
}


@Composable
private fun WeatherGlanceDetailedSuccessContent(
    state: WeatherGlanceUiState.Success,
    iconSize: Dp,
    horizontalAlignment: Alignment.Horizontal,
    textAlign: TextAlign,
    temperatureFontSize: Float,
    locationFontSize: Float,
    conditionFontSize: Float,
    rangeFontSize: Float,
    showCondition: Boolean,
    showRange: Boolean,
    showDaily: Boolean,
    dailyForecastCount: Int,
    showStatusMessage: Boolean,
    columnSpacing: Dp
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Image(
                provider = ImageProvider(state.iconRes),
                contentDescription = null,
                modifier = GlanceModifier
                    .padding(top = 4.dp, start = 4.dp)
                    .size(iconSize),
            )
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = columnSpacing),
                horizontalAlignment = horizontalAlignment,
            ) {
                Text(
                    text = state.locationLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = locationFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = state.temperatureLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = temperatureFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign,
                    ),
                )
                if (showCondition) {
                    Text(
                        text = state.conditionLabel,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = conditionFontSize.sp,
                            textAlign = textAlign,
                        ),
                        maxLines = 1,
                    )
                }
                if (showRange) {
                    Text(
                        text = state.temperatureRangeLabel,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = rangeFontSize.sp,
                            textAlign = textAlign,
                        ),
                    )
                }
            }
        }
        if (showDaily && state.dailyItems.isNotEmpty()) {
            WeatherGlanceDailyForecastRow(state.dailyItems.take(dailyForecastCount))
        }
    }
}


@Composable
private fun WeatherGlanceDailyForecastRow(items: List<WeatherGlanceDailyItem>) {
    Timber.d("WeatherGlanceDailyForecastRow items.size()=%s", items.size)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.End,
    ) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = GlanceModifier
                    .padding(end = if (index in 0..<items.lastIndex) 4.dp else 0.dp)
                    .width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.dayLabel,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Image(
                    provider = ImageProvider(item.iconRes),
                    modifier = GlanceModifier.size(32.dp),
                    contentDescription = null,
                )
            }
        }
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        Timber.tag("WeatherGlance").d("Manual refresh triggered")
        WeatherGlanceAppWidget().update(context, glanceId)
    }
}

private fun Throwable.toUiError(context: Context): WeatherGlanceUiState.Error {
    return when (this) {
        is WeatherGlanceContentBuilder.BuildException -> {
            val message = when (this) {
                is WeatherGlanceContentBuilder.BuildException.LocationPermission -> context.getString(
                    R.string.widget_error_location_permission,
                )
                is WeatherGlanceContentBuilder.BuildException.LocationProvider -> context.getString(
                    R.string.widget_error_location_provider_disabled,
                )
                else -> context.getString(R.string.main_error_weather_fetch)
            }
            WeatherGlanceUiState.Error(
                message = message,
                debugLabel = buildString {
                    append("stage=")
                    append(stage.name.lowercase(Locale.US))
                    cause?.let { cause ->
                        append(" cause=")
                        append(cause::class.simpleName ?: cause::class.java.simpleName)
                        cause.message?.takeIf { it.isNotBlank() }?.let { msg ->
                            append(" message=")
                            append(msg)
                        }
                    }
                }.ifBlank { null },
            )
        }
        else -> WeatherGlanceUiState.Error(
            message = context.getString(R.string.main_error_weather_fetch),
            debugLabel = this::class.simpleName
                ?.plus(this.message?.let { msg -> " message=$msg" }.orEmpty()),
        )
    }
}