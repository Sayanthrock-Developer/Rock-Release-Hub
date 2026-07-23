package com.sayanthrock.rockreleasehub.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeystoreAccessTokenStoreTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var store: KeystoreAccessTokenStore

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.putString(any(), any()) } returns editor

        store = KeystoreAccessTokenStore(context)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getAccessToken returns null when runCatching fails during decryption`() {
        // Arrange
        // Passing an invalid base64 string will cause Base64.decode to fail
        val encodedPayload = "invalid_base64_payload"
        every { sharedPreferences.getString("github_access_token", null) } returns encodedPayload

        // Act
        val result = store.getAccessToken()

        // Assert
        assertNull(result)

        // Verify that clear() was called (removes the key)
        verify { editor.remove("github_access_token") }
        verify { editor.apply() }
    }

    @Test
    fun `getAccessToken clears storage when token payload is too short`() {
        // Arrange
        val shortPayload = ByteArray(10) // less than GCM_IV_LENGTH_BYTES (12)
        val encodedPayload = Base64.encodeToString(shortPayload, Base64.NO_WRAP)
        every { sharedPreferences.getString("github_access_token", null) } returns encodedPayload

        // Act
        val result = store.getAccessToken()

        // Assert
        assertNull(result)
        verify { editor.remove("github_access_token") }
        verify { editor.apply() }
    }

    @Test
    fun `getAccessToken returns null when there is no stored token`() {
        // Arrange
        every { sharedPreferences.getString("github_access_token", null) } returns null

        // Act
        val result = store.getAccessToken()

        // Assert
        assertNull(result)
    }
}
