package app.kwlee.weatherapp.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kwlee.weatherapp.core.ui.R
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.StarOff
import com.composables.icons.lucide.X

@Composable
fun LocationCard(
    location: FavoriteLocation,
    isBookmarked: Boolean,
    onClick: (FavoriteLocation) -> Unit,
    onBookmarkToggle: (FavoriteLocation) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    bookmarkActiveColor: Color = MaterialTheme.colorScheme.primary,
    bookmarkInactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    elevation: Dp = 2.dp,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(location) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val locationName = location.name.split(",").firstOrNull()?.trim() ?: location.name
                Text(
                    text = locationName.ifBlank { stringResource(id = R.string.default_location_label) },
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val regionInfo = location.name.split(",").drop(1).joinToString(", ").trim()
                    .takeIf { it.isNotBlank() }
                if (regionInfo != null) {
                    Text(
                        text = regionInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(onClick = { onBookmarkToggle(location) }) {
                Icon(
                    imageVector = if (isBookmarked) Lucide.StarOff else Lucide.Star,
                    contentDescription = if (isBookmarked) {
                        stringResource(id = R.string.content_description_favorite_remove)
                    } else {
                        stringResource(id = R.string.content_description_favorite_add)
                    },
                    tint = if (isBookmarked) {
                        bookmarkActiveColor
                    } else {
                        bookmarkInactiveColor
                    },
                )
            }
        }
    }
}

