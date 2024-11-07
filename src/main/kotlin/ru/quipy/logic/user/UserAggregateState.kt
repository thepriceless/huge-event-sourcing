package ru.quipy.logic.project

import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.api.user.UserAggregate
import ru.quipy.api.user.UserCreatedEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

// Service's business logic
class UserAggregateState : AggregateState<String, UserAggregate> {
    lateinit var username: String
    lateinit var firstName: String
    lateinit var middleName: String
    lateinit var lastName: String
    lateinit var password: String

    override fun getId() = username

    @StateTransitionFunc
    fun userCreatedApply(event: UserCreatedEvent) {
        username = event.username
        firstName = event.firstName
        middleName = event.middleName
        lastName = event.lastName
        password = event.password
    }
}
