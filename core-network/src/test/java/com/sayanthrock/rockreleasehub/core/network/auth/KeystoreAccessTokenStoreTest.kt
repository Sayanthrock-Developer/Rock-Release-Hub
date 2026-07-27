package com.sayanthrock.rockreleasehub.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.nio.charset.StandardCharsets
import javax.crypto.spec.GCMParameterSpec

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
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

    @Test
    fun `saveAccessToken throws exception when token is blank`() {
        // Arrange
        val blankToken = "   "

        // Act & Assert
        try {
            store.saveAccessToken(blankToken)
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Access token cannot be blank.", e.message)
        }
    }

    @Test
    fun `saveAccessToken encrypts and stores token successfully`() {
        // Arrange
        val token = "valid_access_token"

        // Mock KeyStore to avoid AndroidKeyStore exception in Robolectric
        mockkStatic(KeyStore::class)
        mockkStatic(KeyGenerator::class)
        mockkStatic(Cipher::class)

        val mockKeyStore = mockk<KeyStore>(relaxed = true)
        val mockSecretKey = mockk<SecretKey>(relaxed = true)
        val mockCipher = mockk<Cipher>(relaxed = true)

        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.getKey("rock_release_hub_github_oauth_key", null) } returns mockSecretKey

        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher

        // mock cipher behavior
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) } returns Unit
        every { mockCipher.iv } returns ByteArray(12) { 1 } // 12 bytes IV

        val fakeEncryptedBytes = "encrypted_token".toByteArray()
        every { mockCipher.doFinal(token.toByteArray(StandardCharsets.UTF_8)) } returns fakeEncryptedBytes

        // Act
        store.saveAccessToken(token)

        // Assert
        verify { editor.putString("github_access_token", any()) }
        verify { editor.apply() }

        // The slot captures the exact encoded payload put in the editor
        val slot = io.mockk.slot<String>()
        verify { editor.putString("github_access_token", capture(slot)) }
        val capturedEncodedString = slot.captured

        // For getting token back, setup decrypt mocks
        every { sharedPreferences.getString("github_access_token", null) } returns capturedEncodedString

        every { mockCipher.init(Cipher.DECRYPT_MODE, mockSecretKey, any<GCMParameterSpec>()) } returns Unit
        every { mockCipher.doFinal(fakeEncryptedBytes) } returns token.toByteArray(StandardCharsets.UTF_8)

        val retrievedToken = store.getAccessToken()
        assertEquals(token, retrievedToken)
    }
}
