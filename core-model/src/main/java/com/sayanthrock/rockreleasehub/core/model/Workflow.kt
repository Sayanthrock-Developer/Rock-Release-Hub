package com.sayanthrock.rockreleasehub.core.model

data class Workflow(
    val id: Long,
    val name: String,
    val path: String,
    val state: String,
    val createdAt: String,
    val updatedAt: String
)

data class WorkflowRun(
    val id: Long,
    val name: String,
    val status: String,
    val conclusion: String?,
    val workflowId: Long,
    val event: String,
    val createdAt: String,
    val updatedAt: String
)
