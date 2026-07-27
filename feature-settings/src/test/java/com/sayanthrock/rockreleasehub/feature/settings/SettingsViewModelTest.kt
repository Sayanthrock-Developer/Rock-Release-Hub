package com.sayanthrock.rockreleasehub.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun `initial state is Success with isDarkMode true`() {
        val viewModel = SettingsViewModel()
        val currentState = viewModel.uiState.value

        assertTrue(currentState is SettingsState.Success)
        assertEquals(true, (currentState as SettingsState.Success).isDarkMode)
    }

    @Test
    fun `toggleDarkMode changes isDarkMode from true to false`() {
        val viewModel = SettingsViewModel()

        viewModel.toggleDarkMode()

        val currentState = viewModel.uiState.value
        assertTrue(currentState is SettingsState.Success)
        assertEquals(false, (currentState as SettingsState.Success).isDarkMode)
    }

    @Test
    fun `toggleDarkMode twice changes isDarkMode back to true`() {
        val viewModel = SettingsViewModel()

        viewModel.toggleDarkMode() // toggles to false
        viewModel.toggleDarkMode() // toggles back to true

        val currentState = viewModel.uiState.value
        assertTrue(currentState is SettingsState.Success)
        assertEquals(true, (currentState as SettingsState.Success).isDarkMode)
    }
}
