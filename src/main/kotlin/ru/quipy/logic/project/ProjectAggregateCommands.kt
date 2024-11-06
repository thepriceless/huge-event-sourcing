package ru.quipy.logic.project

import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.api.project.ProjectUpdatedEvent
import java.util.*

fun ProjectAggregateState.createProject(
    title: String,
    username: UUID,
): ProjectCreatedEvent {
    return ProjectCreatedEvent(
        title = title,
    )
}

fun ProjectAggregateState.createStatus(
    projectId: UUID,
    name: String,
    color: String,
): ProjectUpdatedEvent {
    // TODO: Implement
}

fun ProjectAggregateState.updateStatusOrder(
    projectId: UUID,
    orderedStatuses: List<UUID>,
): ProjectUpdatedEvent {
    // TODO: Implement
}

fun ProjectAggregateState.deleteStatus(
    projectId: UUID,
    statusId: UUID,
): ProjectUpdatedEvent {
    // TODO: Implement
}
