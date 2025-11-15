package app.kwlee.weatherapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.feature.main.ui.MainPermissionEvent
import app.kwlee.weatherapp.feature.main.ui.MainScreen
import app.kwlee.weatherapp.feature.main.ui.MainViewModel
import app.kwlee.weatherapp.feature.bookmarks.ui.BookmarksScreen
import app.kwlee.weatherapp.feature.bookmarks.ui.BookmarksViewModel
import app.kwlee.weatherapp.feature.search.ui.LocationSearchScreen
import app.kwlee.weatherapp.feature.search.ui.SearchLocationViewModel
import app.kwlee.weatherapp.feature.settings.ui.SettingsScreen
import app.kwlee.weatherapp.feature.settings.ui.SettingsViewModel
import app.kwlee.weatherapp.core.ui.R
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Star
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed class WeatherDestination(val route: String) {
    data object Home : WeatherDestination("home")
    data object Search : WeatherDestination("search")
    data object Bookmarks : WeatherDestination("bookmarks")
    data object Settings : WeatherDestination("settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppNavHost(
    permissionEvents: Flow<MainPermissionEvent> = emptyFlow(),
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val bottomDestinations = listOf(
        WeatherDestination.Home,
        WeatherDestination.Bookmarks,
        WeatherDestination.Search,
        WeatherDestination.Settings,
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val bottomRoutes = bottomDestinations.map { it.route }
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        val icon = when (destination) {
                            WeatherDestination.Home -> Lucide.MapPin
                            WeatherDestination.Bookmarks -> Lucide.Star
                            WeatherDestination.Search -> Lucide.Search
                            WeatherDestination.Settings -> Lucide.Settings
                        }
                        val labelRes = when (destination) {
                            WeatherDestination.Home -> R.string.navigation_home
                            WeatherDestination.Bookmarks -> R.string.navigation_bookmarks
                            WeatherDestination.Search -> R.string.search_title
                            WeatherDestination.Settings -> R.string.navigation_settings
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(WeatherDestination.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = null) },
                            label = { Text(text = stringResource(id = labelRes)) },
                            colors = NavigationBarItemDefaults.colors(),
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WeatherDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(WeatherDestination.Home.route) {
                val mainViewModel: MainViewModel = hiltViewModel()
                MainScreen(
                    viewModel = mainViewModel,
                    permissionEvents = permissionEvents,
                )
            }

            composable(WeatherDestination.Bookmarks.route) { entry ->
                val bookmarksViewModel: BookmarksViewModel = hiltViewModel()
                val homeBackStackEntry = remember(entry) {
                    navController.getBackStackEntry(WeatherDestination.Home.route)
                }
                val mainViewModel: MainViewModel = hiltViewModel(homeBackStackEntry)
                BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onApplyLocation = { location: FavoriteLocation ->
                        mainViewModel.refreshWeather(location)
                        navController.navigate(WeatherDestination.Home.route) {
                            popUpTo(WeatherDestination.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBookmarkToggle = bookmarksViewModel::onBookmarkToggle,
                )
            }

            composable(WeatherDestination.Search.route) { entry ->
                val searchViewModel: SearchLocationViewModel = hiltViewModel()
                val homeBackStackEntry = remember(entry) {
                    navController.getBackStackEntry(WeatherDestination.Home.route)
                }
                val mainViewModel: MainViewModel = hiltViewModel(homeBackStackEntry)
                LocationSearchScreen(
                    viewModel = searchViewModel,
                    onApplyLocation = { location: FavoriteLocation ->
                        mainViewModel.refreshWeather(location)
                        navController.navigate(WeatherDestination.Home.route) {
                            popUpTo(WeatherDestination.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(WeatherDestination.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                )
            }
        }
    }
}

