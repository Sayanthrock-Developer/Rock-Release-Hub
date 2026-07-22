package com.sayanthrock.rockreleasehub.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.Serializable

interface GitHubApiService {
    @GET("user/repos")
    suspend fun getRepositories(
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): List<NetworkRepository>

    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<NetworkRelease>

    @GET("repos/{owner}/{repo}/actions/workflows")
    suspend fun getWorkflows(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): NetworkWorkflowsResponse
}

@Serializable
data class NetworkRepository(val id: Long, val name: String, val full_name: String, val owner: NetworkUser, val updated_at: String)

@Serializable
data class NetworkUser(val id: Long, val login: String)

@Serializable
data class NetworkRelease(val id: Long, val name: String?, val tag_name: String, val draft: Boolean)

@Serializable
data class NetworkWorkflowsResponse(val total_count: Int, val workflows: List<NetworkWorkflow>)

@Serializable
data class NetworkWorkflow(val id: Long, val name: String, val state: String)
