package com.sayanthrock.rockreleasehub.core.model

data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val type: String
)
