package com.sayanthrock.rockreleasehub.core.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class NetworkUtilsTest {

    @Test
    fun retryIO_successfulExecutionWithoutRetry_returnsResult() = runTest {
        var attempts = 0
        val result = retryIO(
            times = 3,
            initialDelay = 100,
            maxDelay = 1000
        ) {
            attempts++
            "success"
        }

        assertEquals("success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun retryIO_successfulExecutionAfterRetries_returnsResult() = runTest {
        var attempts = 0
        val result = retryIO(
            times = 3,
            initialDelay = 100,
            maxDelay = 1000
        ) {
            attempts++
            if (attempts < 2) {
                throw Exception("temporary failure")
            }
            "success"
        }

        assertEquals("success", result)
        assertEquals(2, attempts)
    }

    @Test
    fun retryIO_failureAfterMaxRetries_throwsLastException() = runTest {
        var attempts = 0
        var thrownException: Exception? = null

        try {
            retryIO<String>(
                times = 3,
                initialDelay = 100,
                maxDelay = 1000
            ) {
                attempts++
                throw Exception("permanent failure")
            }
        } catch (e: Exception) {
            thrownException = e
        }

        assertEquals(3, attempts)
        assertEquals("permanent failure", thrownException?.message)
    }

    @Test
    fun retryIO_cancellationException_isRethrownImmediately() = runTest {
        var attempts = 0
        var thrownException: Exception? = null

        try {
            retryIO<String>(
                times = 3,
                initialDelay = 100,
                maxDelay = 1000
            ) {
                attempts++
                throw CancellationException("cancelled")
            }
        } catch (e: CancellationException) {
            thrownException = e
        } catch (e: Exception) {
            // Should not be caught here
        }

        assertEquals(1, attempts)
        assertTrue(thrownException is CancellationException)
        assertEquals("cancelled", thrownException?.message)
    }

    @Test
    fun retryIO_shouldRetryFalse_throwsImmediately() = runTest {
        var attempts = 0
        var thrownException: Exception? = null

        try {
            retryIO<String>(
                times = 3,
                initialDelay = 100,
                maxDelay = 1000,
                shouldRetry = { false }
            ) {
                attempts++
                throw Exception("do not retry")
            }
        } catch (e: Exception) {
            thrownException = e
        }

        assertEquals(1, attempts)
        assertEquals("do not retry", thrownException?.message)
    }
}
