package com.sayanthrock.rockreleasehub.core.network.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.HttpURLConnection

class GitHubOAuthDeviceFlowGatewayTest {

    @Test
    fun `postFormWithRetry throws GitHubNetworkException when network error occurs`() = runTest {
        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)
        val mockConnection = mockk<HttpURLConnection>(relaxed = true)

        every { gateway["createPostConnection"](any<String>()) } returns mockConnection
        every { mockConnection.outputStream } throws IOException("Simulated Network Error")

        var caughtException: Exception? = null
        try {
            gateway.requestAuthorization("test_client_id")
        } catch (e: Exception) {
            caughtException = e
        }

        assertTrue("Exception should be GitHubNetworkException but was ${caughtException?.javaClass?.name}", caughtException is GitHubNetworkException)
        val networkException = caughtException as GitHubNetworkException
        assertEquals(
            "Android could not reach github.com. Check Wi-Fi or mobile data, and disable any broken VPN or Private DNS setting, then try again.",
            networkException.message
        )
        // Check if there is an underlying simulated cause anywhere in the chain.
        val messageChain = generateSequence(caughtException as Throwable) { it.cause }
            .map { it.message }
            .toList()
        assertTrue("Expected 'Simulated Network Error' in message chain: $messageChain", messageChain.contains("Simulated Network Error"))
    }
}
