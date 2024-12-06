package ru.quipy.logic.person

import ru.quipy.api.PersonAggregate
import ru.quipy.api.PersonCreatedEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class PersonAggregateState : AggregateState<UUID, PersonAggregate> {
    lateinit var personId: UUID
    lateinit var username: String
    lateinit var firstName: String
    lateinit var middleName: String
    lateinit var lastName: String
    lateinit var userId: UUID

    override fun getId() = personId

    @StateTransitionFunc
    fun personCreatedApply(event: PersonCreatedEvent) {
        personId = event.personId
        username = event.username
        firstName = event.firstName
        middleName = event.middleName
        lastName = event.lastName
        userId = event.userId
    }
}
