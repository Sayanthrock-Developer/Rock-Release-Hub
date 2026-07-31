package com.sayanthrock.rockreleasehub.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tmpFolder.newFile("test_settings.preferences_pb") }
        )
        settingsRepository = SettingsRepository(dataStore)
    }

    @Test
    fun appTheme_default_is_ROCK_OBSIDIAN() = runTest {
        val appTheme = settingsRepository.appTheme.first()
        assertEquals(AppTheme.ROCK_OBSIDIAN, appTheme)
    }

    @Test
    fun appTheme_invalid_value_fallback_to_ROCK_OBSIDIAN() = runTest {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.KEY_APP_THEME] = "INVALID_THEME"
        }
        val appTheme = settingsRepository.appTheme.first()
        assertEquals(AppTheme.ROCK_OBSIDIAN, appTheme)
    }

    @Test
    fun themeMode_invalid_value_fallback_to_SYSTEM() = runTest {
        dataStore.edit { preferences ->
            preferences[SettingsRepository.KEY_THEME_MODE] = "INVALID_MODE"
        }
        val themeMode = settingsRepository.themeMode.first()
        assertEquals(ThemeMode.SYSTEM, themeMode)
    }

    @Test
    fun setAppTheme_is_saved_and_emitted() = runTest {
        settingsRepository.setAppTheme(AppTheme.AMOLED_BLACK)
        val appTheme = settingsRepository.appTheme.first()
        assertEquals(AppTheme.AMOLED_BLACK, appTheme)
    }

    @Test
    fun setThemeMode_is_saved_and_emitted() = runTest {
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val themeMode = settingsRepository.themeMode.first()
        assertEquals(ThemeMode.DARK, themeMode)
    }
}
