package com.sayanthrock.rockreleasehub.feature.releases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseViewModelTest {

    @Test
    fun `uiState is initially Success with mock releases`() {
        val viewModel = ReleaseViewModel()
        val currentState = viewModel.uiState.value

        assertTrue(currentState is ReleaseState.Success)
        val successState = currentState as ReleaseState.Success
        assertEquals(2, successState.releases.size)
        assertEquals("v1.0.0", successState.releases[0].name)
        assertEquals("v1.1.0-beta", successState.releases[1].name)
    }
}
