package ru.quipy.logic.project

import ru.quipy.api.project.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class ProjectAggregateState : AggregateState<UUID, ProjectAggregate> {
    lateinit var projectId: UUID
    lateinit var title: String
    var members = mutableListOf<MemberEntity>()
    var tasks = mutableSetOf<TaskEntity>()
    var statuses = mutableListOf<StatusEntity>()

    override fun getId() = projectId

    companion object {
        const val DEFAULT_STATUS_NAME = "CREATED"
        const val DEFAULT_STATUS_COLOR = "#000000"
    }

    // State transition functions which is represented by the class member function
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
            TaskEntity(
                id = event.taskId,
                projectId = event.projectId,
                title = event.title,
                status = event.statusId,
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
            .status = event.statusId
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
            StatusEntity(
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
            MemberEntity(
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

data class TaskEntity(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    var title: String,
    var status: UUID,
    val assignees: MutableList<UUID>
)

data class StatusEntity(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    val name: String,
    val color: String
)

data class MemberEntity(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
)
