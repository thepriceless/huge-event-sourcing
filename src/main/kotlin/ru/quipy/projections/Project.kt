package ru.quipy.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.quipy.api.*
import ru.quipy.logic.project.*
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToMany

@Service
@AggregateSubscriber(
    aggregateClass = ProjectAggregate::class, subscriberName = "project-projection"
)
class ProjectProjection (
    val projectRepository: ProjectRepository,
    val statusRepository: ProjectStatusRepository,
    val taskRepository: ProjectTaskRepository,
    val memberRepository: ProjectMemberRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(UserProjection::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project:project-projection") {
            `when`(ProjectCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    projectRepository.save(ProjectEntity(
                        event.projectId.toString(),
                        event.title,
                    )
                    )
                }
                logger.info("Update project projection, create project ${event.title}")
            }
            `when`(StatusCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    statusRepository.save(ProjectStatusEntity(
                        event.statusId.toString(),
                        event.projectId.toString(),
                        event.color,
                        event.statusName
                    )
                    )
                }
                logger.info("Update project projection, create status ${event.statusName}")
            }
            `when`(StatusDeletedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    statusRepository.deleteById(event.statusId.toString())
                }
                logger.info("Update project projection, delete status ${event.statusId}")
            }
            `when`(TaskCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    val assignees = memberRepository.findAllById(event.assignees.map { it.toString() })
                    taskRepository.save(ProjectTaskEntity(
                        event.taskId.toString(),
                        event.projectId.toString(),
                        event.title,
                        event.statusId.toString(),
                        assignees
                    )
                    )
                }
                logger.info("Update project projection, create task ${event.title}")
            }
            `when`(TaskStatusUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.statusId = event.statusId.toString()
                    }
                }
                logger.info("Update project projection, update task ${event.taskId}")
            }
            `when`(TaskRenamedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.title = event.title
                    }
                }
                logger.info("Update project projection, update task ${event.title}")
            }
            `when`(MemberAssignedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    taskRepository.findById(event.taskId.toString()).orElse(null)?.let { task ->
                        task.assignees.plus(event.memberId)
                    }
                }
                logger.info("Update project projection, update task ${event.taskId}")
            }
            `when`(MemberCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    memberRepository.save(ProjectMemberEntity(
                        id = event.username.toString(),
                        username = event.username,
                        firstName = event.firstName,
                        middleName = event.middleName,
                        lastName = event.lastName,
                        projectId = event.projectId.toString(),
                    )
                    )
                }
                logger.info("Update project projection, create member ${event.username}")
            }
            `when`(PossibleStatusesUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    event.statuses.forEach { status ->
                        statusRepository.findById(status.toString()).orElse(null)?.let { statusEntity ->
                            statusEntity.projectId = event.projectId.toString()
                        }
                    }
                }
                logger.info("Update project projection, update statuses ${event.statuses}")
            }
        }
    }

    fun getProjectById(projectId: String): ProjectEntity? {
        return projectRepository.findById(projectId).orElse(null)
    }

    fun getTaskByProjectId(projectId: String): List<ProjectTaskEntity>? {
        return taskRepository.findAll().filter { it.projectId == projectId }
    }

    fun getStatusesByProjectId(projectId: String): List<ProjectStatusEntity>? {
        return statusRepository.findAll().filter { it.projectId == projectId }
    }

    fun getAllProjects(): List<ProjectEntity> {
        return projectRepository.findAll() ?: listOf()
    }

    fun getAllTasks(): List<ProjectTaskEntity> {
        return taskRepository.findAll() ?: listOf()
    }

    fun getTaskById(taskId: String): ProjectTaskEntity? {
        return taskRepository.findById(taskId.toString()).orElse(null)
    }

    fun getProjectByUsername(username: String): ProjectEntity? {
        logger.info("Get project by username $username")
        val member = memberRepository.findById(username).orElse(null) ?: return null
        logger.info("Get project for member $member")
        return projectRepository.findById(member.projectId.toString()).orElse(null)
    }

    fun getStatusByTaskId(taskId: String): ProjectStatusEntity? {
        val statusId = taskRepository.findById(taskId.toString()).orElse(null).statusId
        return statusRepository.findById(statusId.toString()).orElse(null)
    }

    fun getTasksByStatusId(statusId: String): List<ProjectTaskEntity> {
        return taskRepository.findAll().filter { it.statusId == statusId }
    }
}

@Entity
data class ProjectEntity(
    @Id
    val projectId: String = "",
    val title: String = "",
)

fun ProjectEntity.toDto(): ProjectDto {
    return ProjectDto(
        id = UUID.fromString(this.projectId),
        title = this.title
    )
}

@Entity
class ProjectTaskEntity(
    @Id
    val id: String = "",
    val projectId: String = "",
    var title: String = "",
    var statusId: String = "",

    @ManyToMany(fetch = FetchType.EAGER)
    val assignees: MutableList<ProjectMemberEntity> = mutableListOf()
)

fun ProjectTaskEntity.toDto(): TaskDto {
    return TaskDto(
        id = UUID.fromString(this.id),
        projectId = UUID.fromString(this.projectId),
        title = this.title,
        statusId = UUID.fromString(this.statusId),
        assignees = this.assignees.map { UUID.fromString(it.id) }.toMutableList()
    )
}

@Entity
data class ProjectStatusEntity(
    @Id
    val id: String = "",
    var projectId: String = "",
    val name: String = "",
    val color: String = ""
)

fun ProjectStatusEntity.toDto(): StatusDto {
    return StatusDto(
        id = UUID.fromString(this.id),
        projectId = UUID.fromString(this.projectId),
        name = this.name,
        color = this.color
    )
}

@Entity
data class ProjectMemberEntity(
    @Id
    val id: String = "",
    val projectId: String = "",
    val username: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
)

fun ProjectMemberEntity.toDto(): MemberDto {
    return MemberDto(
        id = UUID.fromString(this.id),
        username = this.username,
        firstName = this.firstName,
        middleName = this.middleName,
        lastName = this.lastName
    )
}

@Repository
interface ProjectRepository : JpaRepository<ProjectEntity, String>

@Repository
interface ProjectTaskRepository : JpaRepository<ProjectTaskEntity, String>

@Repository
interface ProjectStatusRepository : JpaRepository<ProjectStatusEntity, String>

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMemberEntity, String>
