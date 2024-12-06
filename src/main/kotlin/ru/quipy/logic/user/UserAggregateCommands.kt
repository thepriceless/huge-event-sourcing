package ru.quipy.logic.user

import ru.quipy.api.PersonCreatedEvent
import ru.quipy.api.UserCreatedEvent

fun UserAggregateState.createUser(
    password: String,
): UserCreatedEvent {
    return UserCreatedEvent(
        password = password,
    )
}
