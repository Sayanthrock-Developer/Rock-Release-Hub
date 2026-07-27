package com.sayanthrock.rockreleasehub.feature.releases

import com.sayanthrock.rockreleasehub.core.model.Release
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReleaseViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ReleaseViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState is initialized with success state containing mock releases`() {
        val expectedReleases = listOf(
            Release(1, "v1.0.0", "v1.0.0", "Initial Release", "2024-01-01", "2024-01-01", false, false, emptyList()),
            Release(2, "v1.1.0-beta", "v1.1.0", "Beta Release", "2024-01-02", "2024-01-02", false, true, emptyList())
        )

        val currentState = viewModel.uiState.value

        assertEquals(ReleaseState.Success(expectedReleases), currentState)
    }
}
