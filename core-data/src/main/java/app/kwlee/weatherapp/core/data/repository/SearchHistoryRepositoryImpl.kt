package app.kwlee.weatherapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.kwlee.weatherapp.core.di.IoDispatcher
import app.kwlee.weatherapp.core.domain.repository.SearchHistoryRepository
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray


class SearchHistoryRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SearchHistoryRepository {

    override fun observeHistory(): Flow<List<String>> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                preferences[Keys.SEARCH_HISTORY]?.let(::decodeHistory).orEmpty()
            }
    }

    override suspend fun addQuery(query: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val normalized = query.trim()
            if (normalized.isEmpty()) {
                return@runCatching
            }

            dataStore.edit { preferences ->
                val current = preferences[Keys.SEARCH_HISTORY]?.let(::decodeHistory).orEmpty()
                val filtered = current.filterNot { it.equals(normalized, ignoreCase = true) }
                val updated = listOf(normalized) + filtered
                val limited = updated.subList(0, min(updated.size, MAX_HISTORY_ITEMS))
                preferences[Keys.SEARCH_HISTORY] = encodeHistory(limited)
            }
        }
    }

    override suspend fun removeQuery(query: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val normalized = query.trim()
            if (normalized.isEmpty()) {
                return@runCatching
            }

            dataStore.edit { preferences ->
                val current = preferences[Keys.SEARCH_HISTORY]?.let(::decodeHistory).orEmpty()
                val updated = current.filterNot { it.equals(normalized, ignoreCase = true) }
                if (updated.isEmpty()) {
                    preferences.remove(Keys.SEARCH_HISTORY)
                } else {
                    preferences[Keys.SEARCH_HISTORY] = encodeHistory(updated)
                }
            }
        }
    }

    private fun decodeHistory(serialized: String): List<String> {
        return runCatching {
            val jsonArray = JSONArray(serialized)
            val entries = mutableListOf<String>()
            for (index in 0 until jsonArray.length()) {
                val value = jsonArray.optString(index).trim()
                if (value.isNotEmpty()) {
                    entries += value
                }
            }
            entries
        }.getOrDefault(emptyList())
    }

    private fun encodeHistory(history: List<String>): String {
        val jsonArray = JSONArray()
        history.forEach { entry -> jsonArray.put(entry) }
        return jsonArray.toString()
    }

    private object Keys {
        val SEARCH_HISTORY = stringPreferencesKey("search_history_entries")
    }

    companion object {
        private const val MAX_HISTORY_ITEMS = 5
    }
}

