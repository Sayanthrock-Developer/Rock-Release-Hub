package com.sayanthrock.rockreleasehub.feature.updates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayanthrock.rockreleasehub.core.designsystem.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("App Updates") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is UpdateState.Checking -> LoadingScreen()
                is UpdateState.UpToDate -> Text("App is up to date")
                is UpdateState.UpdateAvailable -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Update Available: \${state.version}")
                        Button(onClick = { /* Download */ }) { Text("Download") }
                    }
                }
                is UpdateState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
