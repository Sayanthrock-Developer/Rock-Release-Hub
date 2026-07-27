package com.sayanthrock.rockreleasehub.core.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkUtilsTest {

    @Test
    fun `retryIO succeeds on first attempt`() = runTest {
        var attempts = 0
        val result = retryIO(times = 3, initialDelay = 100, maxDelay = 1000) {
            attempts++
            "success"
        }
        assertEquals("success", result)
        assertEquals(1, attempts)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `retryIO succeeds after retries`() = runTest {
        var attempts = 0
        val result = retryIO(times = 3, initialDelay = 100, maxDelay = 1000) {
            attempts++
            if (attempts < 3) throw RuntimeException("fail")
            "success"
        }
        assertEquals("success", result)
        assertEquals(3, attempts)
        assertEquals(300L, currentTime)
    }

    @Test
    fun `retryIO throws exception if all attempts fail`() = runTest {
        var attempts = 0
        var exceptionThrown = false
        try {
            retryIO(times = 3, initialDelay = 100, maxDelay = 1000) {
                attempts++
                throw RuntimeException("fail")
            }
        } catch (e: RuntimeException) {
            exceptionThrown = true
            assertEquals("fail", e.message)
        }
        assertTrue(exceptionThrown)
        assertEquals(3, attempts)
        assertEquals(300L, currentTime)
    }

    @Test
    fun `retryIO throws immediately on CancellationException`() = runTest {
        var attempts = 0
        var exceptionThrown = false
        try {
            retryIO(times = 3, initialDelay = 100, maxDelay = 1000) {
                attempts++
                throw CancellationException("cancelled")
            }
        } catch (e: CancellationException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
        assertEquals(1, attempts)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `retryIO throws immediately if shouldRetry returns false`() = runTest {
        var attempts = 0
        var exceptionThrown = false
        try {
            retryIO(
                times = 3,
                initialDelay = 100,
                maxDelay = 1000,
                shouldRetry = { it !is IllegalArgumentException }
            ) {
                attempts++
                throw IllegalArgumentException("invalid")
            }
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
        assertEquals(1, attempts)
        assertEquals(0, currentTime)
    }

    @Test
    fun `retryIO respects maxDelay`() = runTest {
        var attempts = 0
        var exceptionThrown = false
        try {
            retryIO(times = 4, initialDelay = 100, maxDelay = 250) {
                attempts++
                throw RuntimeException("fail")
            }
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
        assertEquals(4, attempts)
        assertEquals(550L, currentTime)
    }
}
