package ru.quipy.controller.model

import ru.quipy.api.member.MemberCreatedEvent
import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.api.project.ProjectUpdatedEvent
import ru.quipy.api.taskstatus.StatusCreatedEvent
import ru.quipy.api.taskstatus.StatusDeletedEvent

data class StatusCreatedResponse(
    val projectUpdatedEvent: ProjectUpdatedEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class ProjectCreatedResponse(
    val projectCreatedEvent: ProjectCreatedEvent,
    val memberCreatedEvent: MemberCreatedEvent,
    val statusCreatedEvent: StatusCreatedEvent,
)

data class StatusDeletedResponse(
    val projectUpdatedEvent: ProjectUpdatedEvent,
    val statusDeletedEvent: StatusDeletedEvent,
)
