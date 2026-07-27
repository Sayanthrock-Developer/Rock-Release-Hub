package com.sayanthrock.rockreleasehub.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
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
    fun `saveAccessToken correctly encrypts and saves token`() {
        // Arrange
        val mockKeyStore = mockk<KeyStore>(relaxed = true)
        val mockSecretKey = mockk<SecretKey>(relaxed = true)
        val mockCipher = mockk<Cipher>(relaxed = true)

        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.getKey(any(), any()) } returns mockSecretKey

        mockkStatic(Cipher::class)
        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher

        val testIv = ByteArray(12) { 1 }
        val testEncryptedData = ByteArray(16) { 2 }
        every { mockCipher.iv } returns testIv
        every { mockCipher.doFinal(any()) } returns testEncryptedData

        // Act
        store.saveAccessToken("test_token")

        // Assert
        verify { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) }

        val slot = slot<String>()
        verify { editor.putString("github_access_token", capture(slot)) }

        val expectedPayload = testIv + testEncryptedData
        val expectedBase64 = Base64.encodeToString(expectedPayload, Base64.NO_WRAP)
        assertEquals(expectedBase64, slot.captured)

        verify { editor.apply() }
    }

    @Test
    fun `saveAccessToken generates key if not found in keystore`() {
        // Arrange
        val mockKeyStore = mockk<KeyStore>(relaxed = true)
        val mockSecretKey = mockk<SecretKey>(relaxed = true)
        val mockCipher = mockk<Cipher>(relaxed = true)
        val mockKeyGenerator = mockk<KeyGenerator>(relaxed = true)

        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        // Simulate key not found
        every { mockKeyStore.getKey(any(), any()) } returns null

        mockkStatic(KeyGenerator::class)
        every { KeyGenerator.getInstance(any(), "AndroidKeyStore") } returns mockKeyGenerator
        every { mockKeyGenerator.generateKey() } returns mockSecretKey

        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } returns mockCipher
        every { mockCipher.iv } returns ByteArray(12) { 1 }
        every { mockCipher.doFinal(any()) } returns ByteArray(16) { 2 }

        // Act
        store.saveAccessToken("test_token")

        // Assert
        verify { mockKeyStore.getKey("rock_release_hub_github_oauth_key", null) }
        verify { mockKeyGenerator.init(any<java.security.spec.AlgorithmParameterSpec>()) }
        verify { mockKeyGenerator.generateKey() }
        verify { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `saveAccessToken throws IllegalArgumentException when token is blank`() {
        // Act
        store.saveAccessToken("   ")
    }
}
