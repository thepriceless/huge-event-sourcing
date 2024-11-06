package ru.quipy.logic.taskstatus

import ru.quipy.api.member.MemberCreatedEvent
import ru.quipy.api.taskstatus.StatusCreatedEvent
import ru.quipy.api.taskstatus.StatusDeletedEvent
import ru.quipy.api.taskstatus.TaskCreatedEvent
import ru.quipy.api.taskstatus.TaskUpdatedEvent
import ru.quipy.logic.project.MemberAggregateState
import java.util.*

fun TaskStatusAggregateState.createTask(
    projectId: UUID,
    title: String,
    statusId: UUID,
    assignees: List<UUID>,
): TaskCreatedEvent {

}

fun TaskStatusAggregateState.createStatus(
    projectId: UUID,
    name: String = TaskStatusAggregateState.DEFAULT_STATUS_NAME,
    color: String = TaskStatusAggregateState.DEFAULT_STATUS_COLOR,
): StatusCreatedEvent {
    // TODO: Implement
}

fun TaskStatusAggregateState.assignMemberToTask(
    taskId: UUID,
    memberId: UUID,
): TaskUpdatedEvent {
    // TODO: Implement
}

fun TaskStatusAggregateState.updateTaskStatus(
    taskId: UUID,
    statusId: UUID,
): TaskUpdatedEvent {
    // TODO: Implement
}

fun TaskStatusAggregateState.updateTaskName(
    taskId: UUID,
    title: String,
): TaskUpdatedEvent {
    // TODO: Implement
}

fun TaskStatusAggregateState.deleteStatus(
    projectId: UUID,
    statusId: UUID,
): StatusDeletedEvent {
    // TODO: Implement
}
