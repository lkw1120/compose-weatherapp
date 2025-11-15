package app.kwlee.weatherapp.core.common

import timber.log.Timber


object AppLogger {

    fun d(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.d(throwable, message)
        } else {
            Timber.d(message)
        }
    }

    fun e(throwable: Throwable, message: String? = null) {
        if (message != null) {
            Timber.e(throwable, message)
        } else {
            Timber.e(throwable)
        }
    }
}

