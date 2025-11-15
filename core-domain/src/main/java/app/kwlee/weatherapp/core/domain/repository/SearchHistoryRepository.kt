package app.kwlee.weatherapp.core.domain.repository

import kotlinx.coroutines.flow.Flow


interface SearchHistoryRepository {

    fun observeHistory(): Flow<List<String>>

    suspend fun addQuery(query: String): Result<Unit>

    suspend fun removeQuery(query: String): Result<Unit>
}

