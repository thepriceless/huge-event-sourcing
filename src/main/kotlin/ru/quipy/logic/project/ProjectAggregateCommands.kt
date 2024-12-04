package ru.quipy.logic.project

import ru.quipy.api.*
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_COLOR
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_NAME
import java.util.*

fun ProjectAggregateState.createProject(
    title: String,
    personId: UUID,
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

fun ProjectAggregateState.assignPersonToTask(
    taskId: UUID,
    personId: UUID,
    projectId: UUID,
): PersonAssignedEvent {
    require(members.any { it.id == personId }) { "Member doesn't exist" }
    val targetTask = tasks.first { it.id == taskId }
    require(targetTask.assignees.none { it == personId }) { "Member is already assigned" }

    return PersonAssignedEvent(
        taskId = taskId,
        memberId = personId,
        projectId = projectId,
    )
}

fun ProjectAggregateState.updateTaskName(
    taskId: UUID,
    title: String,
    projectId: UUID,
): TaskRenamedEvent {
    return TaskRenamedEvent(
        taskId = taskId,
        title = title,
        projectId = projectId,
    )
}

fun ProjectAggregateState.updateTaskStatus(
    taskId: UUID,
    projectId: UUID,
    statusId: UUID,
): TaskStatusUpdatedEvent {
    require(statuses.any { it.id == statusId }) { "Status doesn't exist" }

    return TaskStatusUpdatedEvent(
        taskId = taskId,
        statusId = statusId,
        projectId = projectId,
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
    projectId: UUID
): PossibleStatusesUpdatedEvent {
    return PossibleStatusesUpdatedEvent(
        statuses = orderedStatuses,
        projectId = projectId,
    )
}

fun ProjectAggregateState.deleteStatus(
    statusId: UUID,
    projectId: UUID
): StatusDeletedEvent {
    require(tasks.none { it.statusId == statusId }) { "Status is assigned to tasks" }

    return StatusDeletedEvent(
        statusId = statusId,
        projectId = projectId,
    )
}

fun ProjectAggregateState.addPerson(
    projectId: UUID,
    personId: UUID?,
    username: String?,
    firstName: String?,
    middleName: String?,
    lastName: String?,
): PersonAddedToProjectEvent {
    require(members.none { it.id == personId }) { "Person is already a member" }

    requireNotNull(personId) { "Person doesn't exist" }
    requireNotNull(username) { "Username is required" }
    requireNotNull(firstName) { "First name is required" }
    requireNotNull(middleName) { "Middle name is required" }
    requireNotNull(lastName) { "Last name is required" }

    return PersonAddedToProjectEvent(
        personId = personId,
        projectId = projectId,
        username = username,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
    )
}
