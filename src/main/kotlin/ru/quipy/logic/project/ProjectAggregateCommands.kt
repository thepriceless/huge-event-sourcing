package ru.quipy.logic.project

import ru.quipy.api.*
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_COLOR
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_NAME
import java.util.*

fun ProjectAggregateState.createProject(
    title: String,
    username: String,
): ProjectCreatedEvent {
    return ProjectCreatedEvent(
        title = title,
    )
}

fun ProjectAggregateState.createTask(
    projectId: UUID,
    title: String,
    statusId: UUID,
    assignees: MutableList<UUID>,
): TaskCreatedEvent {
    return TaskCreatedEvent(
        taskId = UUID.randomUUID(),
        projectId = projectId,
        title = title,
        statusId = statusId,
        assignees = assignees,
    )
}

fun ProjectAggregateState.assignMemberToTask(
    taskId: UUID,
    memberId: UUID,
): MemberAssignedEvent {
    require(members.any { it.id == memberId }) { "Member doesn't exist" }
    val targetTask = tasks.first { it.id == taskId }
    require(targetTask.assignees.none { it == memberId }) { "Member is already assigned" }

    return MemberAssignedEvent(
        taskId = taskId,
        memberId = memberId,
    )
}

fun ProjectAggregateState.updateTaskName(
    taskId: UUID,
    title: String,
): TaskRenamedEvent {
    return TaskRenamedEvent(
        taskId = taskId,
        title = title,
    )
}

fun ProjectAggregateState.updateTaskStatus(
    taskId: UUID,
    statusId: UUID,
): TaskStatusUpdatedEvent {
    require(statuses.any { it.id == statusId }) { "Status doesn't exist" }

    return TaskStatusUpdatedEvent(
        taskId = taskId,
        statusId = statusId,
    )
}

fun ProjectAggregateState.createStatus(
    name: String = DEFAULT_STATUS_NAME,
    color: String = DEFAULT_STATUS_COLOR,
): StatusCreatedEvent {
    return StatusCreatedEvent(
        statusId = UUID.randomUUID(),
        projectId = projectId,
        color = color,
        statusName = name,
    )
}

fun ProjectAggregateState.updateStatusOrder(
    orderedStatuses: List<UUID>,
): PossibleStatusesUpdatedEvent {
    return PossibleStatusesUpdatedEvent(
        statuses = orderedStatuses,
    )
}

fun ProjectAggregateState.deleteStatus(
    statusId: UUID,
): StatusDeletedEvent {
    require(tasks.none { it.status == statusId }) { "Status is assigned to tasks" }

    return StatusDeletedEvent(
        statusId = statusId,
    )
}

fun ProjectAggregateState.createMember(
    projectId: UUID,
    username: String,
    firstName: String?,
    middleName: String?,
    lastName: String?,
): MemberCreatedEvent {

    requireNotNull(firstName) { "First name is required" }
    requireNotNull(lastName) { "Last name is required" }
    requireNotNull(middleName) { "Middle name is required" }

    return MemberCreatedEvent(
        projectId = projectId,
        username = username,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        memberId = UUID.randomUUID(),
    )
}
