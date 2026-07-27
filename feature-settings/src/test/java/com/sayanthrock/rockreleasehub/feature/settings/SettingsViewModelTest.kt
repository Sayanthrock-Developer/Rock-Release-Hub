package com.sayanthrock.rockreleasehub.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun `initial state is Success with isDarkMode true`() {
        val viewModel = SettingsViewModel()
        val currentState = viewModel.uiState.value

        assertEquals(SettingsState.Success(isDarkMode = true), currentState)
    }

    @Test
    fun `toggleDarkMode changes isDarkMode from true to false`() {
        val viewModel = SettingsViewModel()

        // Initial state is true
        assertEquals(SettingsState.Success(isDarkMode = true), viewModel.uiState.value)

        // Toggle
        viewModel.toggleDarkMode()

        // State should now be false
        assertEquals(SettingsState.Success(isDarkMode = false), viewModel.uiState.value)
    }

    @Test
    fun `toggleDarkMode changes isDarkMode from false to true`() {
        val viewModel = SettingsViewModel()

        // Toggle to false
        viewModel.toggleDarkMode()
        assertEquals(SettingsState.Success(isDarkMode = false), viewModel.uiState.value)

        // Toggle back to true
        viewModel.toggleDarkMode()
        assertEquals(SettingsState.Success(isDarkMode = true), viewModel.uiState.value)
    }
}
