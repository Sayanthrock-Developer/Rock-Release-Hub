package com.sayanthrock.rockreleasehub.core.network.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

interface AccessTokenStore {
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun clear()
}

@Singleton
class KeystoreAccessTokenStore @Inject constructor(
    @ApplicationContext context: Context
) : AccessTokenStore {

    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun getAccessToken(): String? {
        val encodedPayload = preferences.getString(ACCESS_TOKEN_KEY, null) ?: return null

        return runCatching {
            val payload = Base64.decode(encodedPayload, Base64.NO_WRAP)
            require(payload.size > GCM_IV_LENGTH_BYTES) { "Invalid encrypted token payload." }

            val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val encryptedToken = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
                )
            }

            String(cipher.doFinal(encryptedToken), StandardCharsets.UTF_8)
        }.getOrElse {
            clear()
            null
        }
    }

    override fun saveAccessToken(token: String) {
        require(token.isNotBlank()) { "Access token cannot be blank." }

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val encryptedToken = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))
        val payload = cipher.iv + encryptedToken

        preferences.edit()
            .putString(ACCESS_TOKEN_KEY, Base64.encodeToString(payload, Base64.NO_WRAP))
            .apply()
    }

    override fun clear() {
        preferences.edit().remove(ACCESS_TOKEN_KEY).apply()
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val PREFERENCES_NAME = "rock_release_hub_oauth"
        const val ACCESS_TOKEN_KEY = "github_access_token"
        const val KEY_ALIAS = "rock_release_hub_github_oauth_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
