package com.sayanthrock.rockreleasehub.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayanthrock.rockreleasehub.core.designsystem.component.LoadingScreen

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AuthState.Idle -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "GitHub Login", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.initiateLogin() }) {
                        Text("Sign In with GitHub")
                    }
                }
            }
            is AuthState.Loading -> {
                LoadingScreen()
            }
            is AuthState.DeviceFlowInitiated -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Please open:", style = MaterialTheme.typography.bodyLarge)
                    Text(state.verificationUri, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("And enter code:", style = MaterialTheme.typography.bodyLarge)
                    Text(state.userCode, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { viewModel.simulateSuccess() }) {
                        Text("Simulate Success (Dev Only)")
                    }
                }
            }
            is AuthState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: \${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is AuthState.Success -> {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    onAuthSuccess()
                }
            }
        }
    }
}
