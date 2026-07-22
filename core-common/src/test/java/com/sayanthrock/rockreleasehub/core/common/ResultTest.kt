package com.sayanthrock.rockreleasehub.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun successResultHoldsCorrectData() {
        val result = Result.Success("Test Data")
        assertEquals("Test Data", result.data)
    }

    @Test
    fun errorResultHoldsException() {
        val exception = Exception("Test Exception")
        val result = Result.Error(exception)
        assertEquals(exception, result.exception)
    }

    @Test
    fun loadingResultIsLoading() {
        val result = Result.Loading
        assertTrue(result is Result.Loading)
    }
}
