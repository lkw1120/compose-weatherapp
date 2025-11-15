package app.kwlee.weatherapp.core.data.remote.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class InstantJsonAdapter {
    @ToJson
    fun toJson(value: Instant): String = value.toString()

    @FromJson
    fun fromJson(value: String): Instant = Instant.parse(value)
}

class LocalDateJsonAdapter {
    @ToJson
    fun toJson(value: LocalDate): String = value.toString()

    @FromJson
    fun fromJson(value: String): LocalDate = LocalDate.parse(value)
}

class ZonedDateTimeJsonAdapter {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @ToJson
    fun toJson(value: ZonedDateTime): String = formatter.format(value)

    @FromJson
    fun fromJson(value: String): ZonedDateTime = ZonedDateTime.parse(value, formatter)
}

class ZoneIdJsonAdapter {
    @ToJson
    fun toJson(value: ZoneId): String = value.id

    @FromJson
    fun fromJson(value: String): ZoneId = ZoneId.of(value)
}

