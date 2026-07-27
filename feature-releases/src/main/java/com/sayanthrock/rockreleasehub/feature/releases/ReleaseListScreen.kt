package com.sayanthrock.rockreleasehub.feature.releases

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
fun ReleaseListScreen(
    onReleaseClick: (Long) -> Unit,
    viewModel: ReleaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Releases") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ReleaseState.Loading -> LoadingScreen()
                is ReleaseState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.releases) { release ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onReleaseClick(release.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = release.name ?: release.tagName, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Published: \${release.publishedAt}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                is ReleaseState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
