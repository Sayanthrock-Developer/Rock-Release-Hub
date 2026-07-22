package com.sayanthrock.rockreleasehub.core.network.auth

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceAuthorization(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Long,
    val intervalSeconds: Long
)

interface OAuthDeviceFlowGateway {
    suspend fun requestAuthorization(clientId: String): DeviceAuthorization

    suspend fun awaitAccessToken(
        clientId: String,
        authorization: DeviceAuthorization
    ): String
}

@Singleton
class GitHubOAuthDeviceFlowGateway @Inject constructor() : OAuthDeviceFlowGateway {

    override suspend fun requestAuthorization(clientId: String): DeviceAuthorization =
        withContext(Dispatchers.IO) {
            require(clientId.isNotBlank()) { "GitHub OAuth Client ID is missing." }

            val response = postForm(
                url = DEVICE_CODE_URL,
                fields = mapOf(
                    "client_id" to clientId,
                    "scope" to OAUTH_SCOPES
                )
            )

            DeviceAuthorization(
                deviceCode = response.requiredString("device_code"),
                userCode = response.requiredString("user_code"),
                verificationUri = response.requiredString("verification_uri"),
                expiresInSeconds = response.optLong("expires_in", DEFAULT_EXPIRY_SECONDS),
                intervalSeconds = response.optLong("interval", DEFAULT_INTERVAL_SECONDS)
                    .coerceAtLeast(MINIMUM_INTERVAL_SECONDS)
            )
        }

    override suspend fun awaitAccessToken(
        clientId: String,
        authorization: DeviceAuthorization
    ): String = withContext(Dispatchers.IO) {
        val deadline = SystemClock.elapsedRealtime() + authorization.expiresInSeconds * 1_000L
        var pollingInterval = authorization.intervalSeconds.coerceAtLeast(MINIMUM_INTERVAL_SECONDS)

        while (SystemClock.elapsedRealtime() < deadline) {
            delay(pollingInterval * 1_000L)

            val response = postForm(
                url = ACCESS_TOKEN_URL,
                fields = mapOf(
                    "client_id" to clientId,
                    "device_code" to authorization.deviceCode,
                    "grant_type" to DEVICE_CODE_GRANT_TYPE
                )
            )

            val accessToken = response.optString("access_token")
            if (accessToken.isNotBlank()) {
                return@withContext accessToken
            }

            when (val error = response.optString("error")) {
                "authorization_pending" -> Unit
                "slow_down" -> pollingInterval += SLOW_DOWN_INCREMENT_SECONDS
                "expired_token" -> throw GitHubOAuthException(
                    "The GitHub authorization code expired. Start the sign-in process again."
                )
                "access_denied" -> throw GitHubOAuthException(
                    "GitHub authorization was cancelled or denied."
                )
                else -> {
                    val message = response.optString("error_description")
                        .ifBlank { error }
                        .ifBlank { "GitHub authorization failed." }
                    throw GitHubOAuthException(message)
                }
            }
        }

        throw GitHubOAuthException(
            "The GitHub authorization code expired. Start the sign-in process again."
        )
    }

    private fun postForm(url: String, fields: Map<String, String>): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = NETWORK_TIMEOUT_MILLIS
            readTimeout = NETWORK_TIMEOUT_MILLIS
            doOutput = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        return try {
            val body = fields.entries.joinToString("&") { (key, value) ->
                "${key.urlEncode()}=${value.urlEncode()}"
            }

            connection.outputStream.use { output ->
                output.write(body.toByteArray(StandardCharsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (responseBody.isBlank()) {
                throw GitHubOAuthException(
                    "GitHub returned an empty OAuth response (HTTP $statusCode)."
                )
            }

            val response = JSONObject(responseBody)
            if (statusCode !in 200..299) {
                val message = response.optString("error_description")
                    .ifBlank { response.optString("message") }
                    .ifBlank { "GitHub OAuth request failed (HTTP $statusCode)." }
                throw GitHubOAuthException(message)
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.requiredString(key: String): String {
        return optString(key).takeIf { it.isNotBlank() }
            ?: throw GitHubOAuthException("GitHub OAuth response is missing '$key'.")
    }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString())

    private companion object {
        const val DEVICE_CODE_URL = "https://github.com/login/device/code"
        const val ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token"
        const val DEVICE_CODE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code"
        const val OAUTH_SCOPES = "repo workflow read:user read:org notifications"
        const val USER_AGENT = "Rock-Release-Hub-Android"
        const val NETWORK_TIMEOUT_MILLIS = 15_000
        const val DEFAULT_EXPIRY_SECONDS = 900L
        const val DEFAULT_INTERVAL_SECONDS = 5L
        const val MINIMUM_INTERVAL_SECONDS = 5L
        const val SLOW_DOWN_INCREMENT_SECONDS = 5L
    }
}

class GitHubOAuthException(message: String) : IOException(message)
