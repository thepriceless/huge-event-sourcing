package ru.quipy.logic.project

import ru.quipy.api.member.MemberAggregate
import ru.quipy.api.project.ProjectAggregate
import ru.quipy.domain.AggregateState
import java.util.*

// Service's business logic
class MemberAggregateState : AggregateState<UUID, MemberAggregate> {
    lateinit var projectId: UUID
    lateinit var memberId: UUID
    lateinit var username: String
    lateinit var firstName: String
    lateinit var middleName: String
    lateinit var lastName: String

    override fun getId() = memberId
}

data class MemberEntity(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
)
