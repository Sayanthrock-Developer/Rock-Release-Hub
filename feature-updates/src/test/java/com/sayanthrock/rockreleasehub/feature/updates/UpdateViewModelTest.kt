package com.sayanthrock.rockreleasehub.feature.updates

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateViewModelTest {

    @Test
    fun `uiState initially is UpToDate`() {
        val viewModel = UpdateViewModel()
        assertEquals(UpdateState.UpToDate, viewModel.uiState.value)
    }
}
