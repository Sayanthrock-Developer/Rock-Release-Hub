package com.sayanthrock.rockreleasehub.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun initiateLogin() {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            // Simulate device flow initialization
            kotlinx.coroutines.delay(1000)
            _uiState.value = AuthState.DeviceFlowInitiated(
                userCode = "ABCD-1234",
                verificationUri = "https://github.com/login/device"
            )
        }
    }

    fun simulateSuccess() {
        _uiState.value = AuthState.Success
    }
}

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class DeviceFlowInitiated(val userCode: String, val verificationUri: String) : AuthState
    data class Error(val message: String) : AuthState
    data object Success : AuthState
}
