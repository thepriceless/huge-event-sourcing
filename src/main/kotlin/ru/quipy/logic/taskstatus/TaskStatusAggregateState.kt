package ru.quipy.logic.taskstatus

import ru.quipy.api.taskstatus.TaskStatusAggregate
import ru.quipy.domain.AggregateState
import java.util.*

class TaskStatusAggregateState : AggregateState<UUID, TaskStatusAggregate> {
    private lateinit var id: UUID
    private lateinit var projectId: UUID
    private lateinit var title: String
    private lateinit var status: String
    private var assignees = mutableListOf<UUID>()

    override fun getId() = id

    companion object {
        const val DEFAULT_STATUS_NAME = "CREATED"
        const val DEFAULT_STATUS_COLOR = "#000000"
    }
}

data class TaskEntity(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    val title: String,
    val status: String,
    val assignees: List<UUID>
)

data class StatusEntity(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    val name: String,
    val color: String
)
