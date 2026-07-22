package com.sayanthrock.rockreleasehub.feature.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsIdle() = runTest {
        assertEquals(AuthState.Idle, viewModel.uiState.value)
    }

    @Test
    fun simulateSuccessChangesStateToSuccess() = runTest {
        viewModel.simulateSuccess()
        assertEquals(AuthState.Success, viewModel.uiState.value)
    }

    @Test
    fun initiateLoginEmitsLoadingAndThenDeviceFlowInitiated() = runTest(testDispatcher) {
        viewModel.initiateLogin()
        advanceTimeBy(1001)
        assertTrue(viewModel.uiState.value is AuthState.DeviceFlowInitiated)
    }
}
