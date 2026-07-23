package com.sayanthrock.rockreleasehub.core.network

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> retryIO(
    times: Int,
    initialDelay: Long,
    maxDelay: Long,
    factor: Double = 2.0,
    shouldRetry: (Exception) -> Boolean = { true },
    block: () -> T
): T {
    var currentDelay = initialDelay
    for (attempt in 1 until times) {
        try {
            return block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (!shouldRetry(e)) throw e
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return block()
}
