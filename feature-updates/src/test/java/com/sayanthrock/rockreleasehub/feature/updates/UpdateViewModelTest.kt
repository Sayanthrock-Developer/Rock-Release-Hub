package com.sayanthrock.rockreleasehub.feature.updates

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
class UpdateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: UpdateViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UpdateViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsUpToDate() {
        assertEquals(UpdateState.UpToDate, viewModel.uiState.value)
    }
}
