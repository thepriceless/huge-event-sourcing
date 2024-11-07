package ru.quipy.api.user

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val USER_CREATED_EVENT = "USER_CREATED_EVENT"

@DomainEvent(name = USER_CREATED_EVENT)
class UserCreatedEvent(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val password: String,
) : Event<UserAggregate>(
    name = USER_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)