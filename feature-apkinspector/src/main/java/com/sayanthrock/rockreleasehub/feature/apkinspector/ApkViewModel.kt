package com.sayanthrock.rockreleasehub.feature.apkinspector

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ApkViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<ApkState>(ApkState.Idle)
    val uiState: StateFlow<ApkState> = _uiState.asStateFlow()
}

sealed interface ApkState {
    data object Idle : ApkState
    data object Loading : ApkState
    data class Success(val packageInfo: String) : ApkState
    data class Error(val message: String) : ApkState
}
