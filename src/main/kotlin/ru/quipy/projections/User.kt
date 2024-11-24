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
    aggregateClass = UserAggregate::class, subscriberName = "user-projection"
)
class UserProjection (
    val userRepository: UserRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(UserProjection::class.java)

    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "user:user-projection") {
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

    fun getUser(username: String): UserEntity? {
        logger.info("Get user ${username} request")
        return userRepository.findById(username).orElse(null)
    }

    @SubscribeEvent
    fun userCreatedEventSubscriber(event: UserCreatedEvent) {
        logger.info("User created.\nId: ${event.username}")
    }
}

@Entity
data class UserEntity(
    @Id
    val username: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val password: String = "",
)

@Repository
interface UserRepository : JpaRepository<UserEntity, String>