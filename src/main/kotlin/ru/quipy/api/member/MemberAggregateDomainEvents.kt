package ru.quipy.api.member

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val MEMBER_CREATED_EVENT = "USER_CREATED_EVENT"
const val MEMBER_ADDED_TO_PROJECT_EVENT = "MEMBER_ADDED_TO_PROJECT_EVENT"

@DomainEvent(name = MEMBER_CREATED_EVENT)
class MemberCreatedEvent(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val projectId: UUID,
) : Event<MemberAggregate>(
    name = MEMBER_CREATED_EVENT,
    createdAt = System.currentTimeMillis(),
)

@DomainEvent(name = MEMBER_ADDED_TO_PROJECT_EVENT)
class MemberAddedToProjectEvent(
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val projectId: UUID,
) : Event<MemberAggregate>(
    name = MEMBER_ADDED_TO_PROJECT_EVENT,
    createdAt = System.currentTimeMillis(),
)
