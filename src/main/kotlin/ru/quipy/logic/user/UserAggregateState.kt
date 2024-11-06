package ru.quipy.logic.project

import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.api.user.UserAggregate
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

// Service's business logic
class UserAggregateState : AggregateState<String, UserAggregate> {
    private lateinit var username: String
    private lateinit var firstName: String
    private lateinit var middleName: String
    private lateinit var lastName: String
    private lateinit var password: String

    override fun getId() = username

/*    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        tasks[event.taskId] = TaskEntity(event.taskId, event.taskName, mutableSetOf())
        updatedAt = createdAt
    }*/
}
