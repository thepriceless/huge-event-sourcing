package ru.quipy.logic.project

import ru.quipy.api.member.MemberCreatedEvent
import java.util.*

fun MemberAggregateState.createMember(
    projectId: UUID,
    username: String,
): MemberCreatedEvent {
    return MemberCreatedEvent(
        projectId = projectId,
        username = username,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
    )
}

fun MemberAggregateState.addMemberToProject(
    projectId: UUID,
    username: String,
): MemberCreatedEvent {
    // TODO: Implement
}
