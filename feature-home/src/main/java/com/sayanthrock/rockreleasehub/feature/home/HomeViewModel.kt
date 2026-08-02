package com.sayanthrock.rockreleasehub.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MyWorkItem(
    val id: String,
    val title: String,
    val iconName: String,
    val iconColor: Long
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val myWorkItems = listOf(
        MyWorkItem("issues", "Issues", "Adjust", 0xFF23D160),
        MyWorkItem("pull_requests", "Pull Requests", "CallSplit", 0xFF3273F6),
        MyWorkItem("discussions", "Discussions", "Forum", 0xFF834DF2),
        MyWorkItem("projects", "Projects", "ViewQuilt", 0xFF8A94A6),
        MyWorkItem("top_repos", "Top Repositories", "Book", 0xFF4A4E5A),
        MyWorkItem("organizations", "Organizations", "Domain", 0xFFFF8F3C),
        MyWorkItem("starred", "Starred", "StarOutline", 0xFFFFCA28)
    )

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Success(items = myWorkItems))
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()
}

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val items: List<MyWorkItem>) : HomeState
    data class Error(val message: String) : HomeState
}
