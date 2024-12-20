package ru.quipy.controller.model

import ru.quipy.api.*

data class StatusCreatedResponse(
    val possibleStatusesUpdatedEvent: PossibleStatusesUpdatedEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class ProjectCreatedResponse(
    val projectCreatedEvent: ProjectCreatedEvent,
    val memberCreatedEvent: MemberCreatedEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class StatusDeletedResponse(
    val possibleStatusesUpdatedEvent: PossibleStatusesUpdatedEvent,
    val statusDeletedEvent: StatusDeletedEvent,
)
