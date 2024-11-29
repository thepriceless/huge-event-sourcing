package ru.quipy.logic.project

import ru.quipy.api.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class ProjectAggregateState : AggregateState<UUID, ProjectAggregate> {
    lateinit var projectId: UUID
    lateinit var title: String
    var members = mutableListOf<MemberDto>()
    var tasks = mutableSetOf<TaskDto>()
    var statuses = mutableListOf<StatusDto>()

    override fun getId() = projectId

    companion object {
        const val DEFAULT_STATUS_NAME = "CREATED"
        const val DEFAULT_STATUS_COLOR = "#000000"
    }

    @StateTransitionFunc
    fun projectCreatedApply(event: ProjectCreatedEvent) {
        projectId = event.projectId
        title = event.title
    }

    @StateTransitionFunc
    fun statusesUpdatedApply(event: PossibleStatusesUpdatedEvent) {
        statuses = event.statuses.map { statusId ->
            statuses.first { it.id == statusId }
        }.toMutableList()
    }

    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        tasks.add(
            TaskDto(
                id = event.taskId,
                projectId = event.projectId,
                title = event.title,
                statusId = event.statusId,
                assignees = event.assignees,
            )
        )
    }

    @StateTransitionFunc
    fun memberAssignedToTaskApply(event: MemberAssignedEvent) {
        tasks
            .first { it.id == event.taskId }
            .assignees
            .add(event.memberId)
    }

    @StateTransitionFunc
    fun taskStatusUpdatedApply(event: TaskStatusUpdatedEvent) {
        tasks
            .first { it.id == event.taskId }
            .statusId = event.statusId
    }

    @StateTransitionFunc
    fun taskRenamedApply(event: TaskRenamedEvent) {
        tasks
            .first { it.id == event.taskId }
            .title = event.title
    }

    @StateTransitionFunc
    fun statusCreatedApply(event: StatusCreatedEvent) {
        statuses.add(
            StatusDto(
                id = event.statusId,
                projectId = event.projectId,
                name = event.statusName,
                color = event.color,
            )
        )
    }

    @StateTransitionFunc
    fun memberCreatedApply(event: MemberCreatedEvent) {
        members.add(
            MemberDto(
                id = event.memberId,
                username = event.username,
                firstName = event.firstName,
                middleName = event.middleName,
                lastName = event.lastName,
            )
        )
    }

    @StateTransitionFunc
    fun statusDeletedApply(event: StatusDeletedEvent) {
        statuses.removeIf { it.id == event.statusId }
    }
}

data class ProjectDto (
    val id: UUID = UUID.randomUUID(),
    val title: String,
)

data class TaskDto(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    var title: String,
    var statusId: UUID,
    val assignees: MutableList<UUID>
)

data class StatusDto(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    val name: String,
    val color: String
)

data class MemberDto(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
)
