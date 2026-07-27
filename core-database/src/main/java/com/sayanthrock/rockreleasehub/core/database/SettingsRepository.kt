package com.sayanthrock.rockreleasehub.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val appTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        val themeStr = preferences[KEY_APP_THEME] ?: AppTheme.ROCK_OBSIDIAN.name
        try {
            AppTheme.valueOf(themeStr)
        } catch (e: Exception) {
            AppTheme.ROCK_OBSIDIAN
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.name
        }
    }

    suspend fun setAppTheme(appTheme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = appTheme.name
        }
    }

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
    }
}
