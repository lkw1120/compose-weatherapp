package app.kwlee.weatherapp.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class AppConstantsTest {

    @Test
    fun `Network constants have correct values`() {
        assertEquals("https://api.openweathermap.org/", AppConstants.Network.OPEN_WEATHER_BASE_URL)
        assertEquals("data/3.0/onecall", AppConstants.Network.OPEN_WEATHER_ONE_CALL_PATH)
        assertEquals("geo/1.0/direct", AppConstants.Network.OPEN_WEATHER_GEOCODING_PATH)
        assertEquals("geo/1.0/reverse", AppConstants.Network.OPEN_WEATHER_REVERSE_GEOCODING_PATH)
    }

    @Test
    fun `Widget constants have correct values`() {
        assertEquals(5 * 60 * 1000L, AppConstants.Widget.MIN_UPDATE_INTERVAL_MS)
        assertEquals("weather_widget_refresh_", AppConstants.Widget.WORK_NAME_PREFIX)
        assertEquals(5, AppConstants.Widget.MAX_DAILY_ITEMS)
    }

    @Test
    fun `Location constants have correct values`() {
        assertEquals(5_000L, AppConstants.Location.TIMEOUT_MS)
        assertTrue(AppConstants.Location.TIMEOUT_MS > 0)
    }

    @Test
    fun `Cache constants have correct values`() {
        assertEquals(30L, AppConstants.Cache.WEATHER_CACHE_EXPIRATION_MINUTES)
        assertTrue(AppConstants.Cache.WEATHER_CACHE_EXPIRATION_MINUTES > 0)
    }

    @Test
    fun `Widget min update interval is 5 minutes`() {
        val fiveMinutesInMs = 5 * 60 * 1000L
        assertEquals(fiveMinutesInMs, AppConstants.Widget.MIN_UPDATE_INTERVAL_MS)
    }

    @Test
    fun `Location timeout is 5 seconds`() {
        val fiveSecondsInMs = 5_000L
        assertEquals(fiveSecondsInMs, AppConstants.Location.TIMEOUT_MS)
    }

    @Test
    fun `Cache expiration is 30 minutes`() {
        val thirtyMinutes = 30L
        assertEquals(thirtyMinutes, AppConstants.Cache.WEATHER_CACHE_EXPIRATION_MINUTES)
    }
}

