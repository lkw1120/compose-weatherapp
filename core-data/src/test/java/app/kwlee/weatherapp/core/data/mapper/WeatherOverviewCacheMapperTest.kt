package app.kwlee.weatherapp.core.data.mapper

import app.kwlee.weatherapp.core.data.local.entity.WeatherOverviewCacheEntity
import app.kwlee.weatherapp.core.data.remote.moshi.InstantJsonAdapter
import app.kwlee.weatherapp.core.data.remote.moshi.LocalDateJsonAdapter
import app.kwlee.weatherapp.core.data.remote.moshi.ZonedDateTimeJsonAdapter
import app.kwlee.weatherapp.core.data.remote.moshi.ZoneIdJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test


class WeatherOverviewCacheMapperTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(InstantJsonAdapter())
            .add(LocalDateJsonAdapter())
            .add(ZonedDateTimeJsonAdapter())
            .add(ZoneIdJsonAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `toCacheEntity converts valid json to entity correctly`() {
        val validJson = createValidWeatherOverviewJson()
        val latitude = 37.5665
        val longitude = 126.9780
        val updatedAt = Instant.now()

        val entity = createEntityFromJson(validJson, latitude, longitude, updatedAt.toEpochMilli())

        assertEquals(latitude, entity.latitude, 0.0001)
        assertEquals(longitude, entity.longitude, 0.0001)
        assertEquals(updatedAt.toEpochMilli(), entity.updatedAtEpochMillis)
        assertNotNull("JSON should not be null", entity.overviewJson)
    }

    @Test
    fun `toDomain converts entity to domain correctly`() {
        val validJson = createValidWeatherOverviewJson()
        val updatedAt = Instant.now()
        val entity = createEntityFromJson(validJson, 0.0, 0.0, updatedAt.toEpochMilli())

        val result = entity.toDomain(moshi)

        assertNotNull("Domain should not be null", result)
        if (result != null) {
            val lastUpdatedAt = result.lastUpdatedAt
            if (lastUpdatedAt != null) {
                assertEquals("Seoul", result.locationName)
                assertEquals(20.0, result.temperatureCelsius, 0.01)
                assertEquals(updatedAt.toEpochMilli(), lastUpdatedAt.toEpochMilli())
            }
        }
    }

    @Test
    fun `toDomain returns null when json is invalid`() {
        val entity = WeatherOverviewCacheEntity(
            latitude = 0.0,
            longitude = 0.0,
            overviewJson = "invalid json",
            updatedAtEpochMillis = Instant.now().toEpochMilli(),
        )

        val result = entity.toDomain(moshi)

        assertNull(result)
    }

    @Test
    fun `toDomain returns null when json is empty`() {
        val entity = WeatherOverviewCacheEntity(
            latitude = 0.0,
            longitude = 0.0,
            overviewJson = "",
            updatedAtEpochMillis = Instant.now().toEpochMilli(),
        )

        val result = entity.toDomain(moshi)

        assertNull(result)
    }

    @Test
    fun `toDomain handles json with missing required fields`() {
        val incompleteJson = """{"locationName":"Seoul"}"""
        val entity = createEntityFromJson(incompleteJson, 0.0, 0.0, Instant.now().toEpochMilli())

        val result = entity.toDomain(moshi)

        assertNull("Should return null for incomplete JSON", result)
    }

    @Test
    fun `toDomain sets lastUpdatedAt from updatedAtEpochMillis`() {
        val validJson = createValidWeatherOverviewJson()
        val updatedAt = Instant.now()
        val entity = createEntityFromJson(validJson, 0.0, 0.0, updatedAt.toEpochMilli())

        val result = entity.toDomain(moshi)

        assertNotNull("Domain should not be null", result)
        if (result != null) {
            val lastUpdatedAt = result.lastUpdatedAt
            if (lastUpdatedAt != null) {
                assertEquals("lastUpdatedAt should be set from updatedAtEpochMillis", updatedAt.toEpochMilli(), lastUpdatedAt.toEpochMilli())
            }
        }
    }

    private fun createValidWeatherOverviewJson(): String {
        return """
        {
            "locationName":"Seoul",
            "temperatureCelsius":20.0,
            "feelsLikeCelsius":19.0,
            "conditionDescription":"Clear sky",
            "conditionType":"CLEAR",
            "humidityPercent":50,
            "precipitationMm":0.0,
            "highlights":[],
            "hourly":[],
            "daily":[],
            "timeZone":"Asia/Seoul"
        }
        """.trimIndent()
    }

    private fun createEntityFromJson(
        json: String,
        latitude: Double,
        longitude: Double,
        updatedAtEpochMillis: Long,
    ): WeatherOverviewCacheEntity {
        return WeatherOverviewCacheEntity(
            latitude = latitude,
            longitude = longitude,
            overviewJson = json,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }
}

