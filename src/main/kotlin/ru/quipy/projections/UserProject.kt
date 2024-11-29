package ru.quipy.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserCreatedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.Id


@Service
@AggregateSubscriber(
    aggregateClass = UserAggregate::class, subscriberName = "user-project-projection"
)
class UserProjectProjection (
    val userRepository: UserRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(UserProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "user:user-project-projection") {
            `when`(UserCreatedEvent::class) { event ->
                logger.info("User created: {}", event.username)
            }
            `when`(UserCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    userRepository.save(UserEntity(
                        event.username,
                        event.firstName,
                        event.middleName,
                        event.lastName,
                        event.password
                    )
                    )
                }
                logger.info("Update user projection, create user ${event.username}")
            }
        }
    }

    fun getProjectUsers(projectId: String): List<UserEntity?> {
        logger.info("Get users for project  ${projectId} request")
        return listOf()
    }
}

@Entity
class MemberUser {

}