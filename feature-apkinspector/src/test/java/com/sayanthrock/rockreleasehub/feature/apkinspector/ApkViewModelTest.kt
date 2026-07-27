package com.sayanthrock.rockreleasehub.feature.apkinspector

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ApkViewModelTest {

    private lateinit var viewModel: ApkViewModel

    @Before
    fun setup() {
        viewModel = ApkViewModel()
    }

    @Test
    fun initialStateIsIdle() {
        assertEquals(ApkState.Idle, viewModel.uiState.value)
    }
}
