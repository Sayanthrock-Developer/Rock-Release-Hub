package com.sayanthrock.rockreleasehub.core.model

data class Release(
    val id: Long,
    val name: String?,
    val tagName: String,
    val body: String?,
    val createdAt: String,
    val publishedAt: String?,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<ReleaseAsset>
)

data class ReleaseAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val downloadCount: Int,
    val createdAt: String,
    val browserDownloadUrl: String
)
