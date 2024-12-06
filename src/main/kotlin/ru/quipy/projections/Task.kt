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
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Service
@AggregateSubscriber(
    aggregateClass = ProjectAggregate::class, subscriberName = "task-projection"
)
class TaskProjection (
    val taskRepository: ProjectTaskRepository,
    val personRepository: PersonRepository,
    val statusRepository: StatusRepository,
    val projectRepository: ProjectRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(PersonProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "task:task-projection") {
            `when`(TaskCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.save(ProjectTaskEntity(
                        event.taskId.toString(),
                        event.projectId.toString(),
                        event.title,
                        event.statusId.toString(),
                        event.assignees.map { it.toString() }.toMutableList()
                    )
                    )
                }
                logger.info("Update task projection, create task ${event.title}")
            }
            `when`(TaskStatusUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.statusId = event.statusId.toString()
                    }
                }
                logger.info("Update task projection, update task ${event.taskId}")
            }
            `when`(TaskRenamedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.title = event.title
                    }
                }
                logger.info("Update task projection, update task ${event.title}")
            }
            `when`(PersonAssignedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.assignees.plus(event.personId)
                    }
                }
                logger.info("Update task projection, update task ${event.taskId}")
            }
        }
    }

    fun getTasksByProjectId(projectId: String): List<ProjectTaskEntity>? {
        if (projectRepository.findById(projectId).isEmpty) return null
        return taskRepository.findAllByProjectId(projectId)
    }

    fun getAllTasks(): List<ProjectTaskEntity> {
        return taskRepository.findAll() ?: listOf()
    }

    fun getTaskById(taskId: String): ProjectTaskEntity? {
        return taskRepository.findById(taskId).orElse(null)
    }

    fun getTasksByStatusId(statusId: String): List<ProjectTaskEntity> {
        return taskRepository.findAllByStatusId(statusId)
    }
}

@Entity
class ProjectTaskEntity(
    @Id
    val id: String = "",
    val projectId: String = "",
    var title: String = "",
    var statusId: String = "",
    @ElementCollection
    val assignees: MutableList<String> = mutableListOf()
)

fun ProjectTaskEntity.toDto(): TaskDto {
    return TaskDto(
        id = UUID.fromString(this.id),
        projectId = UUID.fromString(this.projectId),
        title = this.title,
        statusId = UUID.fromString(this.statusId),
        assignees = this.assignees.map { UUID.fromString(it) }.toMutableList()
    )
}

@Repository
interface ProjectTaskRepository : JpaRepository<ProjectTaskEntity, String> {
    fun findAllByProjectId(projectId: String): List<ProjectTaskEntity>
    fun findAllByStatusId(statusId: String): List<ProjectTaskEntity>
}
