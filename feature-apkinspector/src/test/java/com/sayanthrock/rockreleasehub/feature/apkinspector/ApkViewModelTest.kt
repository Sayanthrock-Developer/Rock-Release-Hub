package com.sayanthrock.rockreleasehub.feature.apkinspector

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ApkViewModelTest {

    @Test
    fun initialStateIsIdle() = runTest {
        val viewModel = ApkViewModel()

        viewModel.uiState.test {
            assertEquals(ApkState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
