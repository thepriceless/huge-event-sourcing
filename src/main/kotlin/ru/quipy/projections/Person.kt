package ru.quipy.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.api.PersonAggregate
import ru.quipy.api.PersonCreatedEvent
import ru.quipy.controller.model.PersonResponse
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.Id


@Service
@AggregateSubscriber(
    aggregateClass = PersonAggregate::class, subscriberName = "person-projection"
)
class PersonProjection(
    val personRepository: PersonRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
) {
    private val logger = LoggerFactory.getLogger(PersonProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(PersonAggregate::class, "person:person-projection") {
            `when`(PersonCreatedEvent::class) { event ->
                logger.info("Person created: {}", event.username)
            }
            `when`(PersonCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    personRepository.save(
                        PersonEntity(
                            event.personId.toString(),
                            event.userId.toString(),
                            event.username,
                            event.firstName,
                            event.middleName,
                            event.lastName
                        )
                    )
                }
                logger.info("Update person projection, create person ${event.username}")
            }
        }
    }

    fun getPerson(personId: String): PersonEntity? {
        logger.info("Get person ${personId} request")
        return personRepository.findById(personId).orElse(null)
    }

    fun getPersonByUsername(username: String): PersonEntity? {
        logger.info("Get person by username ${username} request")
        return personRepository.findByUsername(username).orElse(null)
    }

    fun getAllPersons(): List<PersonEntity> {
        logger.info("Get all persons request")
        return personRepository.findAll()
    }
}

@Entity
data class PersonEntity(
    @Id
    val personId: String = "",
    val userId: String = "",
    val username: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = ""
)

fun PersonEntity.toDto() = PersonResponse(
    username = username,
    firstName = firstName,
    middleName = middleName,
    lastName = lastName
)

@Repository
interface PersonRepository : JpaRepository<PersonEntity, String> {
    fun findByUsername(username: String): Optional<PersonEntity>
}
