package com.sayanthrock.rockreleasehub.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import androidx.datastore.preferences.core.edit

class SettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.root, "test_preferences.preferences_pb") }
        )
        settingsRepository = SettingsRepository(dataStore)
    }

    @Test
    fun `themeMode emits SYSTEM default`() = testScope.runTest {
        settingsRepository.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
        }
    }

    @Test
    fun `setThemeMode updates themeMode flow`() = testScope.runTest {
        settingsRepository.setThemeMode(ThemeMode.DARK)

        settingsRepository.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun `invalid themeMode string in preferences falls back to SYSTEM`() = testScope.runTest {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.KEY_THEME_MODE] = "INVALID_THEME_MODE"
        }

        settingsRepository.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
        }
    }

    @Test
    fun `appTheme emits ROCK_OBSIDIAN default`() = testScope.runTest {
        settingsRepository.appTheme.test {
            assertEquals(AppTheme.ROCK_OBSIDIAN, awaitItem())
        }
    }

    @Test
    fun `setAppTheme updates appTheme flow`() = testScope.runTest {
        settingsRepository.setAppTheme(AppTheme.TERMINAL_GREEN)

        settingsRepository.appTheme.test {
            assertEquals(AppTheme.TERMINAL_GREEN, awaitItem())
        }
    }

    @Test
    fun `invalid appTheme string in preferences falls back to ROCK_OBSIDIAN`() = testScope.runTest {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.KEY_APP_THEME] = "INVALID_APP_THEME"
        }

        settingsRepository.appTheme.test {
            assertEquals(AppTheme.ROCK_OBSIDIAN, awaitItem())
        }
    }
}
