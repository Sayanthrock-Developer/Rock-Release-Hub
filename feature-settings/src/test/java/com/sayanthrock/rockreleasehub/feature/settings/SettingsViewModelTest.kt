package com.sayanthrock.rockreleasehub.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel()
    }

    @Test
    fun initialStateIsSuccessWithDarkModeTrue() {
        val expectedState = SettingsState.Success(isDarkMode = true)
        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun toggleDarkMode_changesDarkModeToFalse() {
        // Initial state is true
        viewModel.toggleDarkMode()

        val expectedState = SettingsState.Success(isDarkMode = false)
        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun toggleDarkMode_twice_changesDarkModeBackToTrue() {
        // Initial state is true
        viewModel.toggleDarkMode() // to false
        viewModel.toggleDarkMode() // back to true

        val expectedState = SettingsState.Success(isDarkMode = true)
        assertEquals(expectedState, viewModel.uiState.value)
    }
}
