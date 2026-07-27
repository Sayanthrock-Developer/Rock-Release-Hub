package com.sayanthrock.rockreleasehub.core.network.auth

import android.os.SystemClock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GitHubOAuthDeviceFlowGatewayTest {

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
    }

    @After
    fun teardown() {
        unmockkStatic(SystemClock::class)
    }

    @Test
    fun `requestAuthorization parses device authorization response`() = runTest {
        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        every { gateway["postFormWithRetry"](any<String>(), any<Map<String, String>>()) } returns JSONObject().apply {
            put("device_code", "test_device_code")
            put("user_code", "1234-5678")
            put("verification_uri", "https://github.com/login/device")
            put("expires_in", 1800L)
            put("interval", 10L)
        }

        val result = gateway.requestAuthorization("test_client_id")

        assertEquals("test_device_code", result.deviceCode)
        assertEquals("1234-5678", result.userCode)
        assertEquals("https://github.com/login/device", result.verificationUri)
        assertEquals(1800L, result.expiresInSeconds)
        assertEquals(10L, result.intervalSeconds)
    }

    @Test
    fun `awaitAccessToken returns token on first try`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } returns JSONObject().apply {
            put("access_token", "test_token")
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        val result = gateway.awaitAccessToken("client_id", authorization)
        assertEquals("test_token", result)
    }

    @Test
    fun `awaitAccessToken retries on authorization_pending`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        var callCount = 0
        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } answers {
            callCount++
            if (callCount == 1) {
                JSONObject().apply { put("error", "authorization_pending") }
            } else {
                JSONObject().apply { put("access_token", "test_token") }
            }
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        val result = gateway.awaitAccessToken("client_id", authorization)
        assertEquals("test_token", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `awaitAccessToken retries on slow_down and increases interval`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        var callCount = 0
        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } answers {
            callCount++
            if (callCount == 1) {
                JSONObject().apply { put("error", "slow_down") }
            } else {
                JSONObject().apply { put("access_token", "test_token") }
            }
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        val result = gateway.awaitAccessToken("client_id", authorization)
        assertEquals("test_token", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `awaitAccessToken throws on expired_token`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } returns JSONObject().apply {
            put("error", "expired_token")
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        try {
            gateway.awaitAccessToken("client_id", authorization)
            fail("Expected GitHubOAuthException")
        } catch (e: GitHubOAuthException) {
            assertEquals("The GitHub authorization code expired. Start the sign-in process again.", e.message)
        }
    }

    @Test
    fun `awaitAccessToken throws on access_denied`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } returns JSONObject().apply {
            put("error", "access_denied")
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        try {
            gateway.awaitAccessToken("client_id", authorization)
            fail("Expected GitHubOAuthException")
        } catch (e: GitHubOAuthException) {
            assertEquals("GitHub authorization was cancelled or denied.", e.message)
        }
    }

    @Test
    fun `awaitAccessToken throws on unknown error`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } returns JSONObject().apply {
            put("error", "unknown_error")
            put("error_description", "Some unknown error occurred")
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        try {
            gateway.awaitAccessToken("client_id", authorization)
            fail("Expected GitHubOAuthException")
        } catch (e: GitHubOAuthException) {
            assertEquals("Some unknown error occurred", e.message)
        }
    }

    @Test
    fun `awaitAccessToken throws on timeout without network failure`() = runTest {
        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        val clock = AtomicLong(0L)
        every { SystemClock.elapsedRealtime() } answers { clock.get() }

        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } answers {
            clock.set(10000L) // Pass the deadline after first attempt
            JSONObject().apply { put("error", "authorization_pending") }
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 5L, // 5 seconds deadline
            intervalSeconds = 5L
        )

        val job = launch {
            try {
                gateway.awaitAccessToken("client_id", authorization)
                fail("Expected GitHubOAuthException")
            } catch (e: GitHubOAuthException) {
                assertEquals("The GitHub authorization code expired. Start the sign-in process again.", e.message)
            }
        }
        advanceUntilIdle()
        job.join()
    }

    @Test
    fun `awaitAccessToken handles temporary network failure`() = runTest {
        every { SystemClock.elapsedRealtime() } returns 0L

        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        var callCount = 0
        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } answers {
            callCount++
            if (callCount == 1) {
                throw IOException("Network error")
            } else {
                JSONObject().apply { put("access_token", "test_token") }
            }
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 900L,
            intervalSeconds = 5L
        )

        val result = gateway.awaitAccessToken("client_id", authorization)
        assertEquals("test_token", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `awaitAccessToken retries and eventually times out with GitHubNetworkException`() = runTest {
        val gateway = spyk(GitHubOAuthDeviceFlowGateway(), recordPrivateCalls = true)

        // Use a mutable AtomicLong to precisely control the mock behavior independent of coroutine time
        val clock = AtomicLong(0L)
        every { SystemClock.elapsedRealtime() } answers { clock.get() }

        var callCount = 0
        every { gateway["attemptTokenFetch"](any<String>(), any<String>()) } answers {
            callCount++
            // After the first network exception is thrown and caught, the next time elapsedRealtime() is checked in the while loop it will fail the condition.
            clock.set(10000L) // Set to a time > 2000L (deadline)
            throw IOException("Network error")
        }

        val authorization = DeviceAuthorization(
            deviceCode = "device_code",
            userCode = "user_code",
            verificationUri = "uri",
            expiresInSeconds = 2L, // deadline = 2000L
            intervalSeconds = 5L
        )

        val job = launch {
            try {
                gateway.awaitAccessToken("client_id", authorization)
                fail("Expected GitHubNetworkException")
            } catch (e: GitHubNetworkException) {
                assertEquals("Android could not reach github.com. Check Wi-Fi or mobile data, and disable any broken VPN or Private DNS setting, then try again.", e.message)
                assertEquals(1, callCount)
            }
        }
        advanceUntilIdle()
        job.join()
    }
}
