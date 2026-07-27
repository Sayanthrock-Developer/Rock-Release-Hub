package com.sayanthrock.rockreleasehub.feature.home

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun `initial uiState is Success with dummy data`() = runTest {
        // Arrange
        val viewModel = HomeViewModel()

        // Act
        val state = viewModel.uiState.first()

        // Assert
        assertTrue(state is HomeState.Success)
        val successState = state as HomeState.Success
        assertEquals(
            listOf("Updated Repo A", "Released v1.0", "Workflow Failed"),
            successState.recentActivity
        )
    }
}
