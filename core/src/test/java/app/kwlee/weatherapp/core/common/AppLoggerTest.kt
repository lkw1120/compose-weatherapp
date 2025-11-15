package app.kwlee.weatherapp.core.common

import org.junit.Test
import timber.log.Timber


class AppLoggerTest {

    @Test
    fun `d method can be called with message only`() {
        AppLogger.d("Test message")
    }

    @Test
    fun `d method can be called with message and throwable`() {
        val throwable = Exception("Test exception")
        AppLogger.d("Test message", throwable)
    }

    @Test
    fun `e method can be called with throwable only`() {
        val throwable = Exception("Test exception")
        AppLogger.e(throwable)
    }

    @Test
    fun `e method can be called with throwable and message`() {
        val throwable = Exception("Test exception")
        AppLogger.e(throwable, "Test message")
    }

    @Test
    fun `d method handles null throwable`() {
        AppLogger.d("Test message", null)
    }

    @Test
    fun `e method handles null message`() {
        val throwable = Exception("Test exception")
        AppLogger.e(throwable, null)
    }
}

