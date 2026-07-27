package com.sayanthrock.rockreleasehub.core.network.auth

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.HttpURLConnection

@RunWith(RobolectricTestRunner::class)
class GitHubOAuthDeviceFlowGatewayTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var gateway: GitHubOAuthDeviceFlowGateway

    private lateinit var originalDeviceCodeUrl: String
    private lateinit var originalAccessTokenUrl: String

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        originalDeviceCodeUrl = GitHubOAuthDeviceFlowGateway.DEVICE_CODE_URL
        originalAccessTokenUrl = GitHubOAuthDeviceFlowGateway.ACCESS_TOKEN_URL

        // Reassign the URLs to the mock server
        GitHubOAuthDeviceFlowGateway.DEVICE_CODE_URL = mockWebServer.url("/login/device/code").toString()
        GitHubOAuthDeviceFlowGateway.ACCESS_TOKEN_URL = mockWebServer.url("/login/oauth/access_token").toString()

        gateway = GitHubOAuthDeviceFlowGateway()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        GitHubOAuthDeviceFlowGateway.DEVICE_CODE_URL = originalDeviceCodeUrl
        GitHubOAuthDeviceFlowGateway.ACCESS_TOKEN_URL = originalAccessTokenUrl
    }

    @Test
    fun `requestAuthorization with valid response returns DeviceAuthorization`() = runTest {
        // Arrange
        val responseBody = """
            {
              "device_code": "3584d83530557fdd1f46af8289938c8ef79f9dc5",
              "user_code": "WDJB-MQMH",
              "verification_uri": "https://github.com/login/device",
              "expires_in": 900,
              "interval": 5
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
        )

        // Act
        val result = gateway.requestAuthorization("valid_client_id")

        // Assert
        assertEquals("3584d83530557fdd1f46af8289938c8ef79f9dc5", result.deviceCode)
        assertEquals("WDJB-MQMH", result.userCode)
        assertEquals("https://github.com/login/device", result.verificationUri)
        assertEquals(900L, result.expiresInSeconds)
        assertEquals(5L, result.intervalSeconds)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.body.readUtf8().contains("client_id=valid_client_id"))
    }

    @Test
    fun `requestAuthorization with empty client ID throws IllegalArgumentException`() = runTest {
        // Act & Assert
        try {
            gateway.requestAuthorization("   ")
            assertTrue("Expected IllegalArgumentException but no exception was thrown", false)
        } catch (e: IllegalArgumentException) {
            assertEquals("GitHub OAuth Client ID is missing.", e.message)
        }
    }

    @Test
    fun `requestAuthorization with HTTP error throws GitHubOAuthException`() = runTest {
        // Arrange
        val errorResponse = """
            {
              "error": "unauthorized_client",
              "error_description": "The client is not authorized to request a token using this method."
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody(errorResponse)
        )

        // Act & Assert
        try {
            gateway.requestAuthorization("valid_client_id")
            assertTrue("Expected GitHubOAuthException but no exception was thrown", false)
        } catch (e: GitHubOAuthException) {
            assertEquals("The client is not authorized to request a token using this method.", e.message)
        }
    }

    @Test
    fun `requestAuthorization with empty response throws GitHubOAuthException`() = runTest {
        // Arrange
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("")
        )

        // Act & Assert
        try {
            gateway.requestAuthorization("valid_client_id")
            assertTrue("Expected GitHubOAuthException but no exception was thrown", false)
        } catch (e: GitHubOAuthException) {
            assertEquals("GitHub returned an empty OAuth response (HTTP 200).", e.message)
        }
    }

    @Test
    fun `requestAuthorization missing required field throws GitHubOAuthException`() = runTest {
        // Arrange
        val responseBodyMissingDeviceCode = """
            {
              "user_code": "WDJB-MQMH",
              "verification_uri": "https://github.com/login/device",
              "expires_in": 900,
              "interval": 5
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBodyMissingDeviceCode)
        )

        // Act & Assert
        try {
            gateway.requestAuthorization("valid_client_id")
            assertTrue("Expected GitHubOAuthException but no exception was thrown", false)
        } catch (e: GitHubOAuthException) {
            assertEquals("GitHub OAuth response is missing 'device_code'.", e.message)
        }
    }
}
