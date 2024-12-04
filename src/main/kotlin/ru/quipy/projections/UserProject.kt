package ru.quipy.projections

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.api.PersonAggregate
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import javax.annotation.PostConstruct


@Service
@AggregateSubscriber(
    aggregateClass = PersonAggregate::class, subscriberName = "user-project-projection"
)
class UserProjectProjection (
    val personRepository: PersonRepository,
    val memberRepository: ProjectMemberRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(PersonProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(PersonAggregate::class, "user:user-project-projection") {
        }
    }

    fun getProjectUsers(projectId: String): List<UserEntity> {
        logger.info("Get users for project  ${projectId} request")
        return memberRepository.findAll().filter { it.projectId == projectId }
            .map { it.username }
            .map { personRepository.findById(it).orElse(null) }
    }
}
