package com.sayanthrock.rockreleasehub.core.network.api

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.HttpURLConnection

class GitHubApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: GitHubApiService
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        apiService = retrofit.create(GitHubApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getRepositories parses correctly and hits correct path`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                [
                  {
                    "id": 1296269,
                    "name": "Hello-World",
                    "full_name": "octocat/Hello-World",
                    "owner": {
                      "login": "octocat",
                      "id": 1
                    },
                    "updated_at": "2011-01-26T19:14:43Z"
                  }
                ]
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        val repositories = apiService.getRepositories(sort = "updated", perPage = 30, page = 1)

        assert(repositories.size == 1)
        val repo = repositories.first()
        assert(repo.id == 1296269L)
        assert(repo.name == "Hello-World")
        assert(repo.full_name == "octocat/Hello-World")
        assert(repo.owner.login == "octocat")
        assert(repo.owner.id == 1L)
        assert(repo.updated_at == "2011-01-26T19:14:43Z")

        val request = mockWebServer.takeRequest()
        assert(request.path == "/user/repos?sort=updated&per_page=30&page=1")
    }

    @Test
    fun `getReleases parses correctly and hits correct path`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                [
                  {
                    "id": 1,
                    "tag_name": "v1.0.0",
                    "name": "Initial Release",
                    "draft": false
                  }
                ]
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        val releases = apiService.getReleases("octocat", "Hello-World")

        assert(releases.size == 1)
        val release = releases.first()
        assert(release.id == 1L)
        assert(release.tag_name == "v1.0.0")
        assert(release.name == "Initial Release")
        assert(release.draft == false)

        val request = mockWebServer.takeRequest()
        assert(request.path == "/repos/octocat/Hello-World/releases")
    }

    @Test
    fun `getWorkflows parses correctly and hits correct path`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                  "total_count": 1,
                  "workflows": [
                    {
                      "id": 161335,
                      "name": "CI",
                      "state": "active"
                    }
                  ]
                }
            """.trimIndent())
        mockWebServer.enqueue(mockResponse)

        val response = apiService.getWorkflows("octocat", "Hello-World")

        assert(response.total_count == 1)
        assert(response.workflows.size == 1)
        val workflow = response.workflows.first()
        assert(workflow.id == 161335L)
        assert(workflow.name == "CI")
        assert(workflow.state == "active")

        val request = mockWebServer.takeRequest()
        assert(request.path == "/repos/octocat/Hello-World/actions/workflows")
    }

    @Test
    fun `HTTP errors correctly throw HttpException`() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
            .setBody("Not Found")
        mockWebServer.enqueue(mockResponse)

        try {
            apiService.getRepositories()
            assert(false) { "Expected HttpException to be thrown" }
        } catch (e: HttpException) {
            assert(e.code() == HttpURLConnection.HTTP_NOT_FOUND)
        }
    }
}
