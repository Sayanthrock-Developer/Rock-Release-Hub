package com.sayanthrock.rockreleasehub.feature.home

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun `initial state is Success with recent activity`() {
        val expectedState = HomeState.Success(
            recentActivity = listOf("Updated Repo A", "Released v1.0", "Workflow Failed")
        )
        assertEquals(expectedState, viewModel.uiState.value)
    }
}
