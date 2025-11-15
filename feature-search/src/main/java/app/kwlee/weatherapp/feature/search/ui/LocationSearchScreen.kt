package app.kwlee.weatherapp.feature.search.ui

import app.kwlee.weatherapp.core.ui.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.ui.components.LocationCard
import app.kwlee.weatherapp.feature.weatherpreview.ui.WeatherPreviewBottomSheet
import app.kwlee.weatherapp.feature.weatherpreview.ui.WeatherPreviewViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import app.kwlee.weatherapp.core.theme.WeatherAppTheme
import app.kwlee.weatherapp.feature.search.ui.LocationKey
import app.kwlee.weatherapp.feature.search.ui.LocationSearchUiState
import app.kwlee.weatherapp.feature.search.ui.SearchLocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    viewModel: SearchLocationViewModel = hiltViewModel(),
    weatherPreviewViewModel: WeatherPreviewViewModel = hiltViewModel(),
    onApplyLocation: ((FavoriteLocation) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val previewUiState by weatherPreviewViewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    WeatherPreviewBottomSheet(
        uiState = previewUiState,
        onDismiss = weatherPreviewViewModel::dismiss,
        sheetState = sheetState,
        onApplyLocation = onApplyLocation?.let { apply ->
            { location ->
                weatherPreviewViewModel.dismiss()
                apply(location)
            }
        },
    )

    Scaffold { paddingValues ->
        LocationSearchContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::onSearch,
            onClear = viewModel::clearQuery,
            onHistorySelect = viewModel::onHistorySelect,
            onHistoryDelete = viewModel::onHistoryDelete,
            onBookmarkToggle = viewModel::onBookmarkToggle,
            onLocationPreview = weatherPreviewViewModel::present,
        )
    }
}

@Composable
private fun LocationSearchContent(
    modifier: Modifier = Modifier,
    uiState: LocationSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onHistorySelect: (String) -> Unit,
    onHistoryDelete: (String) -> Unit,
    onBookmarkToggle: (FavoriteLocation) -> Unit,
    onLocationPreview: (FavoriteLocation) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LocationSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            query = uiState.query,
            history = uiState.history,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClear = onClear,
            onHistorySelect = onHistorySelect,
            onHistoryDelete = onHistoryDelete,
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null || uiState.errorType != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorType?.let { stringResource(id = it.toResourceId()) }
                            ?: uiState.errorMessage
                            ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            uiState.results.isEmpty() -> {
                val messageRes = if (uiState.query.isBlank()) {
                    R.string.search_prompt
                } else {
                    R.string.search_results_empty
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(messageRes),
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.results) { location ->
                        val isBookmarked = uiState.favoriteKeys.contains(location.toLocationKey())
                        LocationCard(
                            location = location,
                            isBookmarked = isBookmarked,
                            onClick = onLocationPreview,
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

@Preview
@Composable
private fun LocationSearchContentPreview() {
    WeatherAppTheme {
        LocationSearchContent(
            uiState = LocationSearchUiState(
                query = "Seoul",
                results = listOf(
                    FavoriteLocation(name = "샘플 위치", latitude = 37.5665, longitude = 126.9780),
                ),
                isLoading = false,
                history = listOf("Seoul", "Busan"),
            ),
            onQueryChange = {},
            onSearch = {},
            onClear = {},
            onHistorySelect = {},
            onHistoryDelete = {},
            onBookmarkToggle = {},
            onLocationPreview = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    history: List<String>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onHistorySelect: (String) -> Unit,
    onHistoryDelete: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    DockedSearchBar(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    focusManager.clearFocus()
                    expanded = false
                    onSearch()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(imageVector = Lucide.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            onClear()
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Lucide.X,
                                contentDescription = stringResource(R.string.search_clear),
                            )
                        }
                    }
                },
            )
        },
    ) {
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.search_history_empty),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.search_history_title),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(history, key = { it }) { entry ->
                        Surface(
                            onClick = {
                                onHistorySelect(entry)
                                expanded = false
                                focusManager.clearFocus()
                            },
                            color = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = true),
                                )

                                IconButton(
                                    onClick = { onHistoryDelete(entry) },
                                    modifier = Modifier.size(16.dp),
                                ) {
                                    Icon(
                                        imageVector = Lucide.X,
                                        contentDescription = stringResource(R.string.search_history_delete),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FavoriteLocation.toLocationKey(): LocationKey = LocationKey(latitude, longitude)

