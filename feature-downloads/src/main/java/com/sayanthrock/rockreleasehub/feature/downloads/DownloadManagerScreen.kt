package com.sayanthrock.rockreleasehub.feature.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Downloads") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active downloads")
        }
    }
}
