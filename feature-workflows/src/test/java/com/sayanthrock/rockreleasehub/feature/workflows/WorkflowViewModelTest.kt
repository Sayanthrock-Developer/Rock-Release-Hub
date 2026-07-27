package com.sayanthrock.rockreleasehub.feature.workflows

import app.cash.turbine.test
import com.sayanthrock.rockreleasehub.core.model.Workflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkflowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WorkflowViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WorkflowViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsSuccessWithMockWorkflows() = runTest {
        val expectedWorkflows = listOf(
            Workflow(1, "Android Build", "build.yml", "active", "2024-01-01", "2024-01-02"),
            Workflow(2, "Release APK", "release.yml", "active", "2024-01-01", "2024-01-02")
        )

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(WorkflowState.Success(expectedWorkflows), initialState)
        }
    }
}
