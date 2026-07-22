package com.sayanthrock.rockreleasehub.feature.updates

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<UpdateState>(UpdateState.UpToDate)
    val uiState: StateFlow<UpdateState> = _uiState.asStateFlow()
}

sealed interface UpdateState {
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class UpdateAvailable(val version: String, val releaseNotes: String) : UpdateState
    data class Error(val message: String) : UpdateState
}
