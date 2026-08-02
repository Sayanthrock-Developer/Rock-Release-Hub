package com.sayanthrock.rockreleasehub.feature.home

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun `initial uiState is Success with correct initial data`() = runTest {
        // Arrange
        val viewModel = HomeViewModel()

        // Act
        val state = viewModel.uiState.first()

        // Assert
        assertTrue(state is HomeState.Success)
        val successState = state as HomeState.Success

        val expectedItems = listOf(
            MyWorkItem("issues", "Issues", "Adjust", 0xFF23D160),
            MyWorkItem("pull_requests", "Pull Requests", "CallSplit", 0xFF3273F6),
            MyWorkItem("discussions", "Discussions", "Forum", 0xFF834DF2),
            MyWorkItem("projects", "Projects", "ViewQuilt", 0xFF8A94A6),
            MyWorkItem("top_repos", "Top Repositories", "Book", 0xFF4A4E5A),
            MyWorkItem("organizations", "Organizations", "Domain", 0xFFFF8F3C),
            MyWorkItem("starred", "Starred", "StarOutline", 0xFFFFCA28)
        )

        assertEquals(expectedItems, successState.items)
    }
}
