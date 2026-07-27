package com.sayanthrock.rockreleasehub.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayanthrock.rockreleasehub.core.database.SettingsRepository
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        settingsRepository.themeMode,
        settingsRepository.appTheme
    ) { themeMode, appTheme ->
        SettingsState.Success(
            themeMode = themeMode,
            appTheme = appTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState.Loading
    )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setAppTheme(appTheme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(appTheme)
        }
    }
}

sealed interface SettingsState {
    data object Loading : SettingsState
    data class Success(
        val themeMode: ThemeMode,
        val appTheme: AppTheme
    ) : SettingsState
}
