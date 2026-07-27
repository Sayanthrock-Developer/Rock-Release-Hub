package com.sayanthrock.rockreleasehub.feature.settings

import app.cash.turbine.test
import com.sayanthrock.rockreleasehub.core.database.SettingsRepository
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.themeMode } returns flowOf(ThemeMode.DARK)
        every { settingsRepository.appTheme } returns flowOf(AppTheme.AMOLED_BLACK)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state resolves to correct theme and mode`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is SettingsState.Loading)

            val successState = awaitItem()
            assertTrue(successState is SettingsState.Success)
            assertEquals(ThemeMode.DARK, (successState as SettingsState.Success).themeMode)
            assertEquals(AppTheme.AMOLED_BLACK, successState.appTheme)
        }
    }

    @Test
    fun `setThemeMode calls repository`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        coVerify { settingsRepository.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `setAppTheme calls repository`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)

        viewModel.setAppTheme(AppTheme.SUNSET_ALLOY)
        advanceUntilIdle()

        coVerify { settingsRepository.setAppTheme(AppTheme.SUNSET_ALLOY) }
    }
}
