package com.sayanthrock.rockreleasehub.feature.workflows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayanthrock.rockreleasehub.core.designsystem.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowListScreen(
    onWorkflowClick: (Long) -> Unit,
    viewModel: WorkflowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workflows") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is WorkflowState.Loading -> LoadingScreen()
                is WorkflowState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.workflows) { workflow ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onWorkflowClick(workflow.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = workflow.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "State: \${workflow.state}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                is WorkflowState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
