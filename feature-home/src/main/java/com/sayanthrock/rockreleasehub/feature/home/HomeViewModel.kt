package com.sayanthrock.rockreleasehub.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Success(recentActivity = listOf("Updated Repo A", "Released v1.0", "Workflow Failed")))
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()
}

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val recentActivity: List<String>) : HomeState
    data class Error(val message: String) : HomeState
}
