package app.kwlee.weatherapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SearchHistoryRepositoryImplTest {

    private lateinit var mockDataStore: MockDataStore
    private lateinit var repository: SearchHistoryRepositoryImpl
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        mockDataStore = MockDataStore()
        repository = SearchHistoryRepositoryImpl(mockDataStore, testDispatcher)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `observeHistory returns empty list when preferences empty`() = runTest(testDispatcher) {
        mockDataStore.setPreferences(emptyPreferences())

        val result = repository.observeHistory().first()

        assertEquals(emptyList<String>(), result)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `observeHistory returns history from preferences`() = runTest(testDispatcher) {
        val mutablePreferences = emptyPreferences().toMutablePreferences()
        mutablePreferences[stringPreferencesKey("search_history_entries")] = "[\"Seoul\",\"Busan\"]"
        mockDataStore.setPreferences(mutablePreferences)

        // 실제 Preferences 값 확인
        val actualPreferences = mockDataStore.data.first()
        val historyValue = actualPreferences[stringPreferencesKey("search_history_entries")]
        println("History value from preferences: $historyValue")
        
        val result = repository.observeHistory().first()
        println("Observed history result: $result")

        assertEquals(listOf("Seoul", "Busan"), result)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `addQuery adds query to history`() = runTest(testDispatcher) {
        mockDataStore.setPreferences(emptyPreferences())

        val result = repository.addQuery("Seoul")

        if (result.isFailure) {
            println("addQuery failed: ${result.exceptionOrNull()}")
            result.exceptionOrNull()?.printStackTrace()
        }
        assertTrue("addQuery should succeed: ${result.exceptionOrNull()}", result.isSuccess)
        assertEquals(1, mockDataStore.editCallCount)
        
        // updateData가 완료된 후 Flow가 업데이트되었는지 확인
        val updatedHistory = repository.observeHistory().first()
        assertEquals("History should contain Seoul", listOf("Seoul"), updatedHistory)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `addQuery does not add empty query`() = runTest(testDispatcher) {
        mockDataStore.setPreferences(emptyPreferences())

        val result = repository.addQuery("   ")

        assertTrue(result.isSuccess)
        assertEquals(0, mockDataStore.editCallCount)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `addQuery removes duplicates and adds to front`() = runTest(testDispatcher) {
        val mutablePreferences = emptyPreferences().toMutablePreferences()
        mutablePreferences[stringPreferencesKey("search_history_entries")] = "[\"Busan\",\"Incheon\"]"
        mockDataStore.setPreferences(mutablePreferences)

        val result = repository.addQuery("Busan")

        assertTrue(result.isSuccess)
        assertEquals(1, mockDataStore.editCallCount)
        
        val updatedHistory = repository.observeHistory().first()
        assertEquals(listOf("Busan", "Incheon"), updatedHistory)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `removeQuery removes query from history`() = runTest(testDispatcher) {
        val mutablePreferences = emptyPreferences().toMutablePreferences()
        mutablePreferences[stringPreferencesKey("search_history_entries")] = "[\"Seoul\",\"Busan\"]"
        mockDataStore.setPreferences(mutablePreferences)

        val result = repository.removeQuery("Seoul")

        assertTrue(result.isSuccess)
        assertEquals(1, mockDataStore.editCallCount)
        
        val updatedHistory = repository.observeHistory().first()
        assertEquals(listOf("Busan"), updatedHistory)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `addQuery returns failure when dataStore throws exception`() = runTest(testDispatcher) {
        val exception = Exception("DataStore error")
        mockDataStore.setException(exception)

        val result = repository.addQuery("Seoul")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `removeQuery returns failure when dataStore throws exception`() = runTest(testDispatcher) {
        val exception = Exception("DataStore error")
        mockDataStore.setException(exception)

        val result = repository.removeQuery("Seoul")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private class MockDataStore : DataStore<Preferences> {
        private var _preferences: Preferences = emptyPreferences()
        private var exception: Throwable? = null
        var editCallCount = 0
        private val _data = MutableStateFlow(_preferences)

        fun setPreferences(preferences: Preferences) {
            _preferences = preferences
            _data.value = preferences
        }

        fun setException(exception: Throwable) {
            this.exception = exception
        }

        override val data: Flow<Preferences> = _data

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            editCallCount++
            if (exception != null) throw exception!!
            val mutablePreferences = _preferences.toMutablePreferences()
            val updated = transform(mutablePreferences)
            _preferences = updated
            _data.value = updated
            return updated
        }
    }
}

