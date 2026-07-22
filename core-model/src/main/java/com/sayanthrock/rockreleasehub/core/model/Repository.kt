package com.sayanthrock.rockreleasehub.core.model

data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val owner: User,
    val language: String?,
    val stargazersCount: Int,
    val isPrivate: Boolean,
    val updatedAt: String
)
