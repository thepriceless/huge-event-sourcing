package ru.quipy.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Aggregate
import ru.quipy.domain.Event
import java.util.*

const val PROJECT_CREATED_EVENT = "PROJECT_CREATED_EVENT"
const val STATUS_CREATED_EVENT = "STATUS_CREATED_EVENT"
const val TASK_CREATED_EVENT = "TASK_CREATED_EVENT"
const val TASK_STATUS_UPDATED_EVENT = "TASK_STATUS_UPDATED_EVENT"
const val STATUS_DELETED_EVENT = "STATUS_DELETED_EVENT"
const val MEMBER_CREATED_EVENT = "USER_CREATED_EVENT"
const val STATUSES_UPDATED_EVENT = "STATUSES_UPDATED_EVENT"
const val MEMBER_ASSIGNED_EVENT = "MEMBER_ASSIGNED_EVENT"
const val TASK_RENAMED_EVENT = "TASK_RENAMED_EVENT"

@AggregateType(aggregateEventsTableName = "aggregate-project")
class ProjectAggregate : Aggregate

@DomainEvent(name = PROJECT_CREATED_EVENT)
class ProjectCreatedEvent(
    val projectId: UUID = UUID.randomUUID(),
    val title: String,
) : Event<ProjectAggregate>(
    name = PROJECT_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = STATUSES_UPDATED_EVENT)
class PossibleStatusesUpdatedEvent(
    val statuses: List<UUID>,
) : Event<ProjectAggregate>(
    name = STATUSES_UPDATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = STATUS_CREATED_EVENT)
class StatusCreatedEvent(
    val statusId: UUID,
    val projectId: UUID,
    val color: String,
    val statusName: String,
) : Event<ProjectAggregate>(
    name = STATUS_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = STATUS_DELETED_EVENT)
class StatusDeletedEvent(
    val statusId: UUID,
) : Event<ProjectAggregate>(
    name = STATUS_DELETED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = TASK_CREATED_EVENT)
class TaskCreatedEvent(
    val taskId: UUID,
    val projectId: UUID,
    val title: String,
    val statusId: UUID,
    val assignees: MutableList<UUID>,
) : Event<ProjectAggregate>(
    name = TASK_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = TASK_STATUS_UPDATED_EVENT)
class TaskStatusUpdatedEvent(
    val taskId: UUID,
    val statusId: UUID,
) : Event<ProjectAggregate>(
    name = TASK_STATUS_UPDATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = TASK_RENAMED_EVENT)
class TaskRenamedEvent(
    val taskId: UUID,
    val title: String,
) : Event<ProjectAggregate>(
    name = TASK_RENAMED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = MEMBER_ASSIGNED_EVENT)
class MemberAssignedEvent(
    val memberId: UUID,
    val taskId: UUID,
) : Event<ProjectAggregate>(
    name = MEMBER_ASSIGNED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = MEMBER_CREATED_EVENT)
class MemberCreatedEvent(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val projectId: UUID,
    val memberId: UUID,
) : Event<ProjectAggregate>(
    name = MEMBER_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)
