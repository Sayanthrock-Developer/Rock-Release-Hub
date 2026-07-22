package com.sayanthrock.rockreleasehub.feature.repositories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayanthrock.rockreleasehub.core.designsystem.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    onRepoClick: (Long) -> Unit,
    viewModel: RepoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Repositories") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is RepoState.Loading -> LoadingScreen()
                is RepoState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.repos) { repo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onRepoClick(repo.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = repo.language ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                is RepoState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
