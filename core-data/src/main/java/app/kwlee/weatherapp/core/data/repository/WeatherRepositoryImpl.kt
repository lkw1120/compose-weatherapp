package app.kwlee.weatherapp.core.data.repository

import app.kwlee.weatherapp.core.common.AppConstants
import app.kwlee.weatherapp.core.data.local.dao.WeatherOverviewCacheDao
import app.kwlee.weatherapp.core.data.mapper.toCacheEntity
import app.kwlee.weatherapp.core.data.mapper.toDomain
import app.kwlee.weatherapp.core.data.mapper.toDomain as airPollutionToDomain
import app.kwlee.weatherapp.core.data.remote.OpenWeatherApiService
import app.kwlee.weatherapp.core.data.remote.dto.GeocodingLocationDto
import app.kwlee.weatherapp.core.data.remote.dto.ReverseGeocodingDto
import app.kwlee.weatherapp.core.domain.model.AirPollution
import app.kwlee.weatherapp.core.domain.model.FavoriteLocation
import app.kwlee.weatherapp.core.domain.model.WeatherOverview
import app.kwlee.weatherapp.core.domain.repository.WeatherRepository
import app.kwlee.weatherapp.core.di.IoDispatcher
import app.kwlee.weatherapp.core.common.AppLogger
import com.squareup.moshi.Moshi
import java.time.Duration
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


class WeatherRepositoryImpl @Inject constructor(
    private val apiService: OpenWeatherApiService,
    private val weatherOverviewCacheDao: WeatherOverviewCacheDao,
    private val moshi: Moshi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:Named("OpenWeatherApiKey") private val apiKey: String,
) : WeatherRepository {

    override suspend fun fetchWeatherOverview(location: FavoriteLocation): Result<WeatherOverview> =
        withContext(ioDispatcher) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("OpenWeather API key is missing"))
            }

            val now = Instant.now()
            val cachedEntity = weatherOverviewCacheDao.findByCoordinates(
                latitude = location.latitude,
                longitude = location.longitude,
            )
            val cachedOverview = cachedEntity?.toDomain(moshi)
            val cachedInstant = cachedOverview?.lastUpdatedAt
            val isValidCache = cachedInstant != null &&
                cachedInstant.isAfter(now.minus(CACHE_EXPIRATION_DURATION))

            if (cachedOverview != null && isValidCache) {
                return@withContext Result.success(cachedOverview)
            }

            val languageCode = resolveLanguageCode()

            val fetchResult = runCatching {
                val resolvedName = if (location.name.isNotBlank()) {
                    location.name
                } else {
                    runCatching {
                        apiService.reverseGeocode(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            apiKey = apiKey,
                            limit = 1,
                            language = languageCode,
                        ).firstOrNull()?.toDisplayName()
                    }.getOrNull()
                }

                val effectiveLocation = if (!resolvedName.isNullOrBlank()) {
                    location.copy(name = resolvedName)
                } else {
                    location
                }

                val airPollution = runCatching {
                    apiService.fetchAirPollution(
                        latitude = effectiveLocation.latitude,
                        longitude = effectiveLocation.longitude,
                        apiKey = apiKey,
                    ).airPollutionToDomain()
                }.getOrNull()

                val oneCallResponse = apiService.fetchOneCallWeather(
                    latitude = effectiveLocation.latitude,
                    longitude = effectiveLocation.longitude,
                    apiKey = apiKey,
                    language = languageCode,
                )
                AppLogger.d("OneCall API response - alerts: ${oneCallResponse.alerts?.size ?: 0}, alerts data: ${oneCallResponse.alerts}")
                val overview = oneCallResponse.toDomain(effectiveLocation, airPollution) ?: error("Failed to map weather response")

                val displayName = resolvedName
                    ?: overview.locationName.takeIf { it.isNotBlank() }
                    ?: effectiveLocation.name.takeIf { it.isNotBlank() }
                    ?: ""

                val updatedInstant = Instant.now()
                val finalOverview = overview.copy(
                    locationName = displayName,
                    airPollution = airPollution ?: AirPollution(pm25 = 0.0),
                    lastUpdatedAt = updatedInstant,
                )

                finalOverview.toCacheEntity(
                    latitude = effectiveLocation.latitude,
                    longitude = effectiveLocation.longitude,
                    moshi = moshi,
                    updatedAtEpochMillis = updatedInstant.toEpochMilli(),
                )?.let { entity ->
                    weatherOverviewCacheDao.upsert(entity)
                }

                val purgeThreshold = updatedInstant.minus(CACHE_EXPIRATION_DURATION).toEpochMilli()
                weatherOverviewCacheDao.deleteOlderThan(purgeThreshold)

                finalOverview
            }

            fetchResult.recoverCatching { throwable ->
                if (cachedOverview != null) {
                    cachedOverview
                } else {
                    if (cachedEntity != null) {
                        weatherOverviewCacheDao.deleteOlderThan(
                            now.minus(CACHE_EXPIRATION_DURATION).toEpochMilli(),
                        )
                    }
                    throw throwable
                }
            }
        }


    override suspend fun searchLocations(query: String, limit: Int): Result<List<FavoriteLocation>> =
        withContext(ioDispatcher) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("OpenWeather API key is missing"))
            }

            val languageCode = resolveLanguageCode()

            runCatching {
                apiService.geocode(
                    query = query,
                    limit = limit,
                    apiKey = apiKey,
                    language = languageCode,
                ).mapNotNull { it.toFavoriteLocation() }
            }
        }

    override suspend fun clearCache(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            weatherOverviewCacheDao.deleteAll()
        }
    }
}


private val CACHE_EXPIRATION_DURATION: Duration =
    Duration.ofMinutes(AppConstants.Cache.WEATHER_CACHE_EXPIRATION_MINUTES)

/**
 * Maps locale language codes to OpenWeather API language codes.
 * Supports all languages available in OpenWeather API.
 */
private val LOCALE_TO_API_LANGUAGE = mapOf(
    "sq" to "sq",  // Albanian
    "af" to "af",  // Afrikaans
    "ar" to "ar",  // Arabic
    "az" to "az",  // Azerbaijani
    "eu" to "eu",  // Basque
    "be" to "be",  // Belarusian
    "bg" to "bg",  // Bulgarian
    "ca" to "ca",  // Catalan
    "hr" to "hr",  // Croatian
    "cs" to "cz",  // Czech (locale uses "cs", API uses "cz")
    "da" to "da",  // Danish
    "nl" to "nl",  // Dutch
    "en" to "en",  // English
    "fi" to "fi",  // Finnish
    "fr" to "fr",  // French
    "gl" to "gl",  // Galician
    "de" to "de",  // German
    "el" to "el",  // Greek
    "he" to "he",  // Hebrew
    "hi" to "hi",  // Hindi
    "hu" to "hu",  // Hungarian
    "is" to "is",  // Icelandic
    "id" to "id",  // Indonesian
    "it" to "it",  // Italian
    "ja" to "ja",  // Japanese
    "ko" to "kr",  // Korean (locale uses "ko", API uses "kr")
    "ku" to "ku",  // Kurmanji (Kurdish)
    "lv" to "la",  // Latvian (locale uses "lv", API uses "la")
    "lt" to "lt",  // Lithuanian
    "mk" to "mk",  // Macedonian
    "no" to "no",  // Norwegian
    "fa" to "fa",  // Persian (Farsi)
    "pl" to "pl",  // Polish
    "pt" to "pt",  // Portuguese
    "ro" to "ro",  // Romanian
    "ru" to "ru",  // Russian
    "sr" to "sr",  // Serbian
    "sk" to "sk",  // Slovak
    "sl" to "sl",  // Slovenian
    "es" to "es",  // Spanish (sp is also valid, but es is standard)
    "sv" to "sv",  // Swedish (se is also valid, but sv is standard)
    "th" to "th",  // Thai
    "tr" to "tr",  // Turkish
    "uk" to "ua",  // Ukrainian (locale uses "uk", API uses "ua")
    "vi" to "vi",  // Vietnamese
    "zu" to "zu",  // Zulu
)

private fun resolveLanguageCode(): String {
    val locale = Locale.getDefault()
    val language = locale.language.lowercase(Locale.ROOT)
    val country = locale.country.uppercase(Locale.ROOT)
    
    // Handle special cases with country-specific variants
    return when {
        // Chinese: zh_CN -> zh_cn, zh_TW -> zh_tw
        language == "zh" -> when (country) {
            "TW" -> "zh_tw"
            else -> "zh_cn"
        }
        // Portuguese: pt_BR -> pt_br, pt -> pt (from map)
        language == "pt" && country == "BR" -> "pt_br"
        // All other languages: check map or default to English
        else -> LOCALE_TO_API_LANGUAGE[language] ?: "en"
    }
}

private fun ReverseGeocodingDto.toDisplayName(): String {
    return buildDisplayName(name, localNames, state, country)
}


private fun GeocodingLocationDto.toDisplayName(): String {
    return buildDisplayName(name, localNames, state, country)
}


private fun GeocodingLocationDto.toFavoriteLocation(): FavoriteLocation? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    val displayName = toDisplayName().takeIf { it.isNotBlank() } ?: return null
    return FavoriteLocation(
        id = null,
        name = displayName,
        latitude = lat,
        longitude = lon,
        isPrimary = false,
    )
}


private fun buildDisplayName(
    localizedName: String?,
    localNames: Map<String, String>?,
    state: String?,
    country: String?,
): String {
    val locale = Locale.getDefault()
    val language = locale.language.lowercase(Locale.ROOT)
    
    // localNames uses ISO 639-1 language codes (e.g., "ko", "en", "zh")
    // not API language codes (e.g., "kr", "zh_cn")
    val isoLanguageCode = when (language) {
        "zh" -> {
            val country = locale.country.uppercase(Locale.ROOT)
            if (country == "TW") "zh_tw" else "zh_cn"
        }
        "pt" -> {
            val country = locale.country.uppercase(Locale.ROOT)
            if (country == "BR") "pt_br" else "pt"
        }
        else -> language
    }
    
    val primaryName = localNames?.get(isoLanguageCode)
        ?: localNames?.get(language)
        ?: localNames?.get("en")
        ?: localizedName
        ?: ""

    val stateLabel = state
        ?.takeIf { it.isNotBlank() && !primaryName.equals(it, ignoreCase = true) }

    val countryLabel = country

    return listOf(primaryName, stateLabel, countryLabel)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(separator = ", ")
}

