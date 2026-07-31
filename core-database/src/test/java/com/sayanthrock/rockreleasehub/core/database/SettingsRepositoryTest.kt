package com.sayanthrock.rockreleasehub.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_settings.preferences_pb") }
        )
        repository = SettingsRepository(dataStore)
    }

    @Test
    fun themeMode_returnsSystem_whenInvalidStringIsStored() = testScope.runTest {
        // Arrange: Store an invalid string in DataStore for KEY_THEME_MODE
        dataStore.edit { preferences ->
            preferences[SettingsRepository.KEY_THEME_MODE] = "INVALID_THEME_MODE"
        }

        // Act & Assert: Verify that the flow emits ThemeMode.SYSTEM
        repository.themeMode.test {
            val mode = awaitItem()
            assertEquals(ThemeMode.SYSTEM, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
