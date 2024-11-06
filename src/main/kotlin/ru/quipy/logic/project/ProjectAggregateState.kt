package ru.quipy.logic.project

import ru.quipy.api.project.ProjectAggregate
import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.logic.taskstatus.StatusEntity
import ru.quipy.logic.taskstatus.TaskEntity
import java.util.*

class ProjectAggregateState : AggregateState<UUID, ProjectAggregate> {
    private lateinit var projectId: UUID
    private lateinit var title: String
    private var members = mutableListOf<MemberEntity>()
    private var tasks = mutableSetOf<TaskEntity>()
    private var statuses = mutableListOf<StatusEntity>()

    override fun getId() = projectId

/*    // State transition functions which is represented by the class member function
    @StateTransitionFunc
    fun projectCreatedApply(event: ProjectCreatedEvent) {
        projectId = event.projectId
        projectTitle = event.title
        creatorId = event.creatorId
        updatedAt = createdAt
    }

    @StateTransitionFunc
    fun tagCreatedApply(event: TagCreatedEvent) {
        projectTags[event.tagId] = TagEntity(event.tagId, event.tagName)
        updatedAt = createdAt
    }

    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        tasks[event.taskId] = TaskEntity(event.taskId, event.taskName, mutableSetOf())
        updatedAt = createdAt
    }*/
}
