package ru.quipy.api.taskstatus

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import ru.quipy.logic.StatusEntity
import java.util.*

const val STATUS_CREATED_EVENT = "STATUS_CREATED_EVENT"
const val TASK_CREATED_EVENT = "TASK_CREATED_EVENT"
const val TASK_UPDATED_EVENT = "TASK_UPDATED_EVENT"
const val STATUS_DELETED_EVENT = "STATUS_DELETED_EVENT"

@DomainEvent(name = STATUS_CREATED_EVENT)
class StatusCreatedEvent(
    val statusId: UUID,
    val projectId: UUID,
    val color: String,
    val statusName: String,
) : Event<TaskStatusAggregate>(
    name = STATUS_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = STATUS_DELETED_EVENT)
class StatusDeletedEvent(
    val statusId: UUID,
) : Event<TaskStatusAggregate>(
    name = STATUS_DELETED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = TASK_CREATED_EVENT)
class TaskCreatedEvent(
    val taskId: UUID,
    val projectId: UUID,
    val title: String,
    val status: StatusEntity,
    val assignees: List<UUID>,
) : Event<TaskStatusAggregate>(
    name = TASK_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = TASK_UPDATED_EVENT)
class TaskUpdatedEvent(
    val taskId: UUID,
    val projectId: UUID,
    val title: String,
    val status: StatusEntity,
    val assignees: List<UUID>,
) : Event<TaskStatusAggregate>(
    name = TASK_UPDATED_EVENT,
    createdAt = System.currentTimeMillis(),
)
