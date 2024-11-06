package ru.quipy.api.project

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val PROJECT_CREATED_EVENT = "PROJECT_CREATED_EVENT"
const val PROJECT_UPDATED_EVENT = "PROJECT_UPDATED_EVENT"

@DomainEvent(name = PROJECT_CREATED_EVENT)
class ProjectCreatedEvent(
    val projectId: UUID = UUID.randomUUID(),
    val title: String,
) : Event<ProjectAggregate>(
    name = PROJECT_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = PROJECT_UPDATED_EVENT)
class ProjectUpdatedEvent(
    val projectId: UUID,
    val title: String,
    val orderedStatuses: List<UUID>,
) : Event<ProjectAggregate>(
    name = PROJECT_UPDATED_EVENT,
    createdAt = System.currentTimeMillis(),
)
