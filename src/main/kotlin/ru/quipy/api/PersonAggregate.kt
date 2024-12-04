package ru.quipy.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Aggregate
import ru.quipy.domain.Event
import java.util.*

const val PERSON_CREATED_EVENT = "PERSON_CREATED_EVENT"

@AggregateType(aggregateEventsTableName = "aggregate-person")
class PersonAggregate : Aggregate

@DomainEvent(name = PERSON_CREATED_EVENT)
class PersonCreatedEvent(
    val personId: UUID = UUID.randomUUID(),
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val userId: UUID,
) : Event<PersonAggregate>(
    name = PERSON_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)
