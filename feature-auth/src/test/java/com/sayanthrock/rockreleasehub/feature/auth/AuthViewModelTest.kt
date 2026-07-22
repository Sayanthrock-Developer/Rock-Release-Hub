package com.sayanthrock.rockreleasehub.feature.auth

import com.sayanthrock.rockreleasehub.core.network.auth.AccessTokenStore
import com.sayanthrock.rockreleasehub.core.network.auth.DeviceAuthorization
import com.sayanthrock.rockreleasehub.core.network.auth.OAuthDeviceFlowGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var gateway: FakeOAuthDeviceFlowGateway
    private lateinit var tokenStore: FakeAccessTokenStore
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeOAuthDeviceFlowGateway()
        tokenStore = FakeAccessTokenStore()
        viewModel = AuthViewModel(gateway, tokenStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsIdleWithoutStoredToken() {
        assertEquals(AuthState.Idle, viewModel.uiState.value)
    }

    @Test
    fun storedTokenStartsAuthenticated() {
        val authenticatedViewModel = AuthViewModel(
            gateway,
            FakeAccessTokenStore(initialToken = "stored-token")
        )

        assertEquals(AuthState.Success, authenticatedViewModel.uiState.value)
    }

    @Test
    fun successfulDeviceFlowStoresTokenAndAuthenticates() = runTest(testDispatcher) {
        viewModel.initiateLogin()
        advanceUntilIdle()

        assertEquals("github-token", tokenStore.getAccessToken())
        assertEquals(AuthState.Success, viewModel.uiState.value)
    }

    @Test
    fun oauthFailureShowsErrorState() = runTest(testDispatcher) {
        gateway.failure = IllegalStateException("Authorization denied")

        viewModel.initiateLogin()
        advanceUntilIdle()

        assertEquals(
            AuthState.Error("Authorization denied"),
            viewModel.uiState.value
        )
    }
}

private class FakeOAuthDeviceFlowGateway : OAuthDeviceFlowGateway {
    var failure: Exception? = null

    override suspend fun requestAuthorization(clientId: String): DeviceAuthorization {
        failure?.let { throw it }
        return DeviceAuthorization(
            deviceCode = "device-code",
            userCode = "ABCD-1234",
            verificationUri = "https://github.com/login/device",
            expiresInSeconds = 900,
            intervalSeconds = 5
        )
    }

    override suspend fun awaitAccessToken(
        clientId: String,
        authorization: DeviceAuthorization
    ): String {
        failure?.let { throw it }
        return "github-token"
    }
}

private class FakeAccessTokenStore(
    initialToken: String? = null
) : AccessTokenStore {
    private var token: String? = initialToken

    override fun getAccessToken(): String? = token

    override fun saveAccessToken(token: String) {
        this.token = token
    }

    override fun clear() {
        token = null
    }
}
