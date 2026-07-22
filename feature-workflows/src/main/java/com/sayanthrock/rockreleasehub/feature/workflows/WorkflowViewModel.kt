package com.sayanthrock.rockreleasehub.feature.workflows

import androidx.lifecycle.ViewModel
import com.sayanthrock.rockreleasehub.core.model.Workflow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WorkflowViewModel @Inject constructor() : ViewModel() {
    private val mockWorkflows = listOf(
        Workflow(1, "Android Build", "build.yml", "active", "2024-01-01", "2024-01-02"),
        Workflow(2, "Release APK", "release.yml", "active", "2024-01-01", "2024-01-02")
    )

    private val _uiState = MutableStateFlow<WorkflowState>(WorkflowState.Success(mockWorkflows))
    val uiState: StateFlow<WorkflowState> = _uiState.asStateFlow()
}

sealed interface WorkflowState {
    data object Loading : WorkflowState
    data class Success(val workflows: List<Workflow>) : WorkflowState
    data class Error(val message: String) : WorkflowState
}
