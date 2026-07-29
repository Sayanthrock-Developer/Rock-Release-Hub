package com.sayanthrock.rockreleasehub.core.network.auth

import io.mockk.every
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GitHubOAuthDeviceFlowGatewayTest {

    private lateinit var gateway: GitHubOAuthDeviceFlowGateway

    @Before
    fun setup() {
        gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `postFormWithRetry throws GitHubNetworkException on persistent network failure`() = runBlocking {
        val ioException = IOException("Simulated network failure")
        every {
            gateway invoke "postForm" withArguments listOf(any<String>(), any<Map<String, String>>())
        } throws ioException

        var caughtException: Exception? = null
        try {
            runBlocking {
                gateway.requestAuthorization("test_client_id")
            }
        } catch (e: Exception) {
            caughtException = e
        }

        assertTrue(caughtException is GitHubNetworkException)
        assertEquals(
            "Android could not reach github.com. Check Wi-Fi or mobile data, and disable any broken VPN or Private DNS setting, then try again.",
            caughtException?.message
        )
        assertTrue(caughtException?.cause is GitHubNetworkException)
        assertEquals("Simulated network failure", caughtException?.cause?.cause?.message)

        verify(exactly = 4) {
            gateway invoke "postForm" withArguments listOf(any<String>(), any<Map<String, String>>())
        }
    }

    @Test
    fun `postFormWithRetry throws GitHubOAuthException immediately without retry`() = runBlocking {
        val oauthException = GitHubOAuthException("OAuth error")
        every {
            gateway invoke "postForm" withArguments listOf(any<String>(), any<Map<String, String>>())
        } throws oauthException

        var caughtException: Exception? = null
        try {
            runBlocking {
                gateway.requestAuthorization("test_client_id")
            }
        } catch (e: Exception) {
            caughtException = e
        }

        assertTrue(caughtException is GitHubOAuthException)
        assertEquals("OAuth error", caughtException?.message)

        verify(exactly = 1) {
            gateway invoke "postForm" withArguments listOf(any<String>(), any<Map<String, String>>())
        }
    }
}
