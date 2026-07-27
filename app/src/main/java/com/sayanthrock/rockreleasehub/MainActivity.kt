package com.sayanthrock.rockreleasehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sayanthrock.rockreleasehub.core.database.SettingsRepository
import com.sayanthrock.rockreleasehub.core.designsystem.theme.RockReleaseHubTheme
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val appTheme by settingsRepository.appTheme.collectAsStateWithLifecycle(initialValue = AppTheme.ROCK_OBSIDIAN)

            RockReleaseHubTheme(
                themeMode = themeMode,
                appTheme = appTheme
            ) {
                AppNavGraph()
            }
        }
    }
}
