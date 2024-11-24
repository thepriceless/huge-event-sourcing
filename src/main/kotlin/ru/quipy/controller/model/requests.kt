package ru.quipy.controller.model

import java.util.UUID

data class CreateProjectRequest(
    val title: String,
    val username: String,
)

data class CreateStatusRequest(
    val name: String,
    val color: String,
)

data class CreateUserRequest(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val password: String,
)

data class LoginUserRequest(
    val username: String,
    val password: String,
)

data class AddMemberToProjectRequest(
    val username: String,
)

data class CreateTaskRequest(
    val projectId: UUID,
    val title: String,
    val statusId: UUID,
    val assignees: List<UUID> = emptyList(),
)

data class AddMemberToTaskRequest(
    val memberId: UUID,
)

data class UpdateTaskStatusRequest(
    val statusId: UUID,
)

data class UpdateTaskNameRequest(
    val title: String,
)

data class UpdateStatusOrderRequest(
    val orderedStatuses: List<UUID>,
)
