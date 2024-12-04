package ru.quipy.logic.person

import ru.quipy.api.PersonCreatedEvent
import java.util.*

fun PersonAggregateState.createPerson(
    username: String,
    firstName: String,
    middleName: String,
    lastName: String,
    userId: UUID,
): PersonCreatedEvent {
    return PersonCreatedEvent(
        username = username,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        userId = userId,
    )
}
