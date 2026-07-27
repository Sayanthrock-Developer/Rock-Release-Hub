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

            val response = postFormWithRetry(
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
        var networkRetryDelayMillis = INITIAL_NETWORK_RETRY_DELAY_MILLIS
        var lastNetworkFailure: IOException? = null

        while (SystemClock.elapsedRealtime() < deadline) {
            delay(pollingInterval * 1_000L)

            val response = try {
                postForm(
                    url = ACCESS_TOKEN_URL,
                    fields = mapOf(
                        "client_id" to clientId,
                        "device_code" to authorization.deviceCode,
                        "grant_type" to DEVICE_CODE_GRANT_TYPE
                    )
                )
            } catch (error: IOException) {
                if (error is GitHubOAuthException) {
                    throw error
                }

                // DNS, timeout and connection failures can happen briefly when the app resumes
                // after the browser authorization step. Keep the approved device code alive and
                // retry instead of immediately discarding the sign-in attempt.
                lastNetworkFailure = error
                delay(networkRetryDelayMillis)
                networkRetryDelayMillis = (networkRetryDelayMillis * 2)
                    .coerceAtMost(MAX_NETWORK_RETRY_DELAY_MILLIS)
                continue
            }

            lastNetworkFailure = null
            networkRetryDelayMillis = INITIAL_NETWORK_RETRY_DELAY_MILLIS

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

        if (lastNetworkFailure != null) {
            throw GitHubNetworkException(
                message = NETWORK_ERROR_MESSAGE,
                cause = lastNetworkFailure
            )
        }

        throw GitHubOAuthException(
            "The GitHub authorization code expired. Start the sign-in process again."
        )
    }

    private suspend fun postFormWithRetry(
        url: String,
        fields: Map<String, String>
    ): JSONObject {
        try {
            return com.sayanthrock.rockreleasehub.core.network.retryIO(
                times = INITIAL_REQUEST_MAX_ATTEMPTS,
                initialDelay = INITIAL_NETWORK_RETRY_DELAY_MILLIS,
                maxDelay = MAX_NETWORK_RETRY_DELAY_MILLIS,
                factor = 2.0,
                shouldRetry = { it is IOException && it !is GitHubOAuthException }
            ) {
                postForm(url, fields)
            }
        } catch (error: IOException) {
            if (error is GitHubOAuthException) {
                throw error
            }
            throw GitHubNetworkException(
                message = NETWORK_ERROR_MESSAGE,
                cause = error
            )
        }
    }

    private fun postForm(url: String, fields: Map<String, String>): JSONObject {
        val connection = createPostConnection(url)

        return try {
            writeFormData(connection, fields)
            handleResponse(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun createPostConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = NETWORK_TIMEOUT_MILLIS
            readTimeout = NETWORK_TIMEOUT_MILLIS
            doOutput = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", USER_AGENT)
        }
    }

    private fun writeFormData(connection: HttpURLConnection, fields: Map<String, String>) {
        val body = fields.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }

        connection.outputStream.use { output ->
            output.write(body.toByteArray(StandardCharsets.UTF_8))
        }
    }

    private fun handleResponse(connection: HttpURLConnection): JSONObject {
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

        return response
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
        const val NETWORK_ERROR_MESSAGE =
            "Android could not reach github.com. Check Wi-Fi or mobile data, and disable any broken VPN or Private DNS setting, then try again."
        const val NETWORK_TIMEOUT_MILLIS = 15_000
        const val DEFAULT_EXPIRY_SECONDS = 900L
        const val DEFAULT_INTERVAL_SECONDS = 5L
        const val MINIMUM_INTERVAL_SECONDS = 5L
        const val SLOW_DOWN_INCREMENT_SECONDS = 5L
        const val INITIAL_REQUEST_MAX_ATTEMPTS = 4
        const val INITIAL_NETWORK_RETRY_DELAY_MILLIS = 2_000L
        const val MAX_NETWORK_RETRY_DELAY_MILLIS = 30_000L
    }
}

class GitHubOAuthException(message: String) : IOException(message)

class GitHubNetworkException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)
