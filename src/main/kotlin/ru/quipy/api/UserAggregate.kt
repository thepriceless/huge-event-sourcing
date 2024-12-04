package ru.quipy.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Aggregate
import ru.quipy.domain.Event
import java.util.UUID

const val USER_CREATED_EVENT = "USER_CREATED_EVENT"

@AggregateType(aggregateEventsTableName = "aggregate-user")
class UserAggregate : Aggregate

@DomainEvent(name = USER_CREATED_EVENT)
class UserCreatedEvent(
    val userId: UUID = UUID.randomUUID(),
    val password: String,
) : Event<UserAggregate>(
    name = USER_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)
