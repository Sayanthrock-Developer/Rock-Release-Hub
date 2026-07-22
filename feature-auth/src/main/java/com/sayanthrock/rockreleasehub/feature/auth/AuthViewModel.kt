package com.sayanthrock.rockreleasehub.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayanthrock.rockreleasehub.core.network.auth.AccessTokenStore
import com.sayanthrock.rockreleasehub.core.network.auth.OAuthDeviceFlowGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val oauthDeviceFlowGateway: OAuthDeviceFlowGateway,
    private val accessTokenStore: AccessTokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(
        if (accessTokenStore.getAccessToken().isNullOrBlank()) {
            AuthState.Idle
        } else {
            AuthState.Success
        }
    )
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private var loginJob: Job? = null

    fun initiateLogin() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _uiState.value = AuthState.Loading

            try {
                val authorization = oauthDeviceFlowGateway.requestAuthorization(
                    clientId = BuildConfig.GITHUB_CLIENT_ID
                )

                _uiState.value = AuthState.DeviceFlowInitiated(
                    userCode = authorization.userCode,
                    verificationUri = authorization.verificationUri,
                    expiresInSeconds = authorization.expiresInSeconds
                )

                val accessToken = oauthDeviceFlowGateway.awaitAccessToken(
                    clientId = BuildConfig.GITHUB_CLIENT_ID,
                    authorization = authorization
                )
                accessTokenStore.saveAccessToken(accessToken)
                _uiState.value = AuthState.Success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = AuthState.Error(
                    error.message ?: "Unable to sign in with GitHub. Please try again."
                )
            }
        }
    }

    fun cancelLogin() {
        loginJob?.cancel()
        loginJob = null
        _uiState.value = AuthState.Idle
    }

    fun retryLogin() {
        initiateLogin()
    }

    fun signOut() {
        loginJob?.cancel()
        accessTokenStore.clear()
        _uiState.value = AuthState.Idle
    }
}

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState

    data class DeviceFlowInitiated(
        val userCode: String,
        val verificationUri: String,
        val expiresInSeconds: Long
    ) : AuthState

    data class Error(val message: String) : AuthState
    data object Success : AuthState
}
