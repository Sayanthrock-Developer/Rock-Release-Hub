package com.sayanthrock.rockreleasehub.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsState>(SettingsState.Success(isDarkMode = true))
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        val currentState = _uiState.value
        if (currentState is SettingsState.Success) {
            _uiState.value = currentState.copy(isDarkMode = !currentState.isDarkMode)
        }
    }
}

sealed interface SettingsState {
    data object Loading : SettingsState
    data class Success(val isDarkMode: Boolean) : SettingsState
}
