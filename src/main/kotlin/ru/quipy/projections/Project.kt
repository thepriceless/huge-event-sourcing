package ru.quipy.projections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.controller.model.PersonResponse
import ru.quipy.logic.project.*
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import java.util.*
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.Id

@Service
@AggregateSubscriber(
    aggregateClass = ProjectAggregate::class, subscriberName = "project-projection"
)
class ProjectProjection (
    val projectRepository: ProjectRepository,
    val taskRepository: ProjectTaskRepository,
    val projectMemberRepository: ProjectMemberRepository,
    val personRepository: PersonRepository,
    val subscriptionsManager: AggregateSubscriptionsManager,
){
    private val logger = LoggerFactory.getLogger(PersonProjection::class.java)

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
            `when`(PersonAddedToProjectEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    val member = projectMemberRepository.findById(event.personId.toString()).orElse(
                        ProjectMemberEntity(
                        event.personId.toString(),
                        listOf(),
                        event.username,
                    )
                    )
                    member.projects.plus(event.projectId)
                    projectMemberRepository.save(member)

                    val project = projectRepository.findById(event.projectId.toString()).get()
                    project.members.plus(event.personId)
                    projectRepository.save(project)
                }
                logger.info("Update project projection, add member ${event.username}")
            }
            `when`(TaskCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    val project = projectRepository.findById(event.projectId.toString()).get()
                    project.tasks.plus(event.taskId)
                    projectRepository.save(project)
                }
                logger.info("Update task projection, create task ${event.title}")
            }
        }
    }

    fun getProjectsByPersonId(personId: String): List<ProjectEntity> {
        val project = projectMemberRepository.findById(personId)
        if (project.isPresent) {
            return project.get().projects.map { projectRepository.findById(it).get() }
        }
        return listOf()
    }

    fun getPersonsByProjectId(projectId: String): List<PersonEntity> {
        val members = projectMemberRepository.findByProjectId(projectId)
        return members.map { personRepository.findById(it.id).get() }
    }

    fun getAllProjects(): List<ProjectEntity> {
        return projectRepository.findAll()
    }
}

@Entity
data class ProjectEntity(
    @Id
    val projectId: String = "",
    val title: String = "",
    val members: List<String> = listOf(),
    val tasks: List<String> = listOf(),
)

fun ProjectEntity.toDto(): ProjectDto {
    return ProjectDto(
        id = UUID.fromString(this.projectId),
        title = this.title
    )
}

@Entity
data class ProjectMemberEntity(
    @Id
    val id: String = "",
    val projects: List<String> = listOf(),
    val username: String = "",
)

@Repository
interface ProjectRepository : JpaRepository<ProjectEntity, String>

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMemberEntity, String> {
    fun findByProjectId(projectId: String): List<ProjectMemberEntity>
}
