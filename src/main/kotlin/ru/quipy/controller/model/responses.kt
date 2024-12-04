package ru.quipy.controller.model

import ru.quipy.api.*

data class StatusCreatedResponse(
    val possibleStatusesUpdatedEvent: PossibleStatusesUpdatedEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class ProjectCreatedResponse(
    val projectCreatedEvent: ProjectCreatedEvent,
    val personAddedToProjectEvent: PersonAddedToProjectEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class StatusDeletedResponse(
    val possibleStatusesUpdatedEvent: PossibleStatusesUpdatedEvent,
    val statusDeletedEvent: StatusDeletedEvent,
)

data class PersonCreatedResponse(
    val personCreatedEvent: PersonCreatedEvent,
    val userCreatedEvent: UserCreatedEvent,
)

data class PersonResponse(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
)
