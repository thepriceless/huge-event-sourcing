package ru.quipy.logic.project

import ru.quipy.api.project.ProjectCreatedEvent
import ru.quipy.api.user.UserCreatedEvent
import java.util.*

fun UserAggregateState.createUser(
    username: String,
    firstName: String,
    middleName: String,
    lastName: String,
    password: String,
) = UserCreatedEvent(
    username = username,
    firstName = firstName,
    middleName = middleName,
    lastName = lastName,
    password = password,
)
