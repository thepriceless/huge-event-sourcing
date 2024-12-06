package ru.quipy.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.logic.project.*
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.Id

@Service
@AggregateSubscriber(
    aggregateClass = ProjectAggregate::class, subscriberName = "status-projection"
)
class StatusProjection (
    val statusRepository: StatusRepository,
    val taskRepository: ProjectTaskRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(PersonProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "status:status-projection") {
            `when`(StatusCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    statusRepository.save(StatusEntity(
                        event.statusId.toString(),
                        event.projectId.toString(),
                        event.color,
                        event.statusName
                    )
                    )
                }
                logger.info("Update status projection, create status ${event.statusName}")
            }
            `when`(StatusDeletedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    statusRepository.deleteById(event.statusId.toString())
                }
                logger.info("Update status projection, delete status ${event.statusId}")
            }
            `when`(PossibleStatusesUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    event.statuses.forEach { status ->
                        statusRepository.findById(status.toString()).orElse(null)?.let { statusEntity ->
                            statusEntity.projectId = event.projectId.toString()
                        }
                    }
                }
                logger.info("Update status projection, update statuses ${event.statuses}")
            }
        }
    }

    fun getStatusesByProjectId(projectId: String): List<StatusEntity>? {
        return statusRepository.findAllByProjectId(projectId)
    }

    fun getStatusByTaskId(taskId: String): StatusEntity? {
        val statusId = taskRepository.findById(taskId.toString()).orElse(null).statusId
        return statusRepository.findById(statusId.toString()).orElse(null)
    }
}

@Entity
data class StatusEntity(
    @Id
    val id: String = "",
    var projectId: String = "",
    val name: String = "",
    val color: String = ""
)

fun StatusEntity.toDto(): StatusDto {
    return StatusDto(
        id = UUID.fromString(this.id),
        projectId = UUID.fromString(this.projectId),
        name = this.name,
        color = this.color
    )
}

@Repository
interface StatusRepository : JpaRepository<StatusEntity, String> {
    fun findAllByProjectId(projectId: String): List<StatusEntity>
}
