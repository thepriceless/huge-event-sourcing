package ru.quipy.logic.user

import ru.quipy.api.UserCreatedEvent
import ru.quipy.logic.project.UserAggregateState

fun UserAggregateState.createUser(
    username: String,
    firstName: String,
    middleName: String,
    lastName: String,
    password: String,
    existingUsername: String?,
): UserCreatedEvent {
    require(existingUsername == null) { "User with username $username already exists" }

    return UserCreatedEvent(
        username = username,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        password = password,
        existingUsername = existingUsername
    )
}
