package com.sayanthrock.rockreleasehub.feature.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sayanthrock.rockreleasehub.core.designsystem.component.LoadingScreen

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            AuthState.Idle -> IdleScreen(onLoginClick = viewModel::initiateLogin)
            AuthState.Loading -> LoadingScreen()
            is AuthState.DeviceFlowInitiated -> DeviceFlowScreen(
                state = state,
                onCancelClick = viewModel::cancelLogin
            )
            is AuthState.Error -> ErrorScreen(
                message = state.message,
                onRetryClick = viewModel::retryLogin
            )
            AuthState.Success -> {
                LaunchedEffect(Unit) {
                    onAuthSuccess()
                }
            }
        }
    }
}
