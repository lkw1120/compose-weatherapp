package app.kwlee.weatherapp.feature.bookmarks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.ui.components.LocationCard
import app.kwlee.weatherapp.feature.weatherpreview.ui.WeatherPreviewBottomSheet
import app.kwlee.weatherapp.feature.weatherpreview.ui.WeatherPreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onApplyLocation: (FavoriteLocation) -> Unit,
    onBookmarkToggle: (FavoriteLocation) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val weatherPreviewViewModel: WeatherPreviewViewModel = hiltViewModel()
    val previewUiState by weatherPreviewViewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    WeatherPreviewBottomSheet(
        uiState = previewUiState,
        onDismiss = weatherPreviewViewModel::dismiss,
        sheetState = sheetState,
        onApplyLocation = { location ->
            weatherPreviewViewModel.dismiss()
            onApplyLocation(location)
        },
    )

    BookmarksScreenContent(
        uiState = uiState,
        onLocationClick = weatherPreviewViewModel::present,
        onBookmarkToggle = onBookmarkToggle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreenContent(
    uiState: BookmarksUiState,
    onLocationClick: (FavoriteLocation) -> Unit,
    onBookmarkToggle: (FavoriteLocation) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = app.kwlee.weatherapp.core.ui.R.string.bookmarks_title)) },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.locations.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(id = app.kwlee.weatherapp.core.ui.R.string.bookmarks_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                val containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                val contentColor = MaterialTheme.colorScheme.primary
                val subtitleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                val bookmarkInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.locations,
                        key = { it.id ?: (it.name + it.latitude + it.longitude) },
                    ) { location ->
                        LocationCard(
                            location = location,
                            isBookmarked = true,
                            onClick = onLocationClick,
                            onBookmarkToggle = onBookmarkToggle,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            subtitleColor = subtitleColor,
                            bookmarkActiveColor = contentColor,
                            bookmarkInactiveColor = bookmarkInactiveColor,
                            elevation = 0.dp,
                        )
                    }
                }
            }
        }
    }
}

