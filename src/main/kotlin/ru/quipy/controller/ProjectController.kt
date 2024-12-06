package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.quipy.api.*
import ru.quipy.api.PersonAggregate
import ru.quipy.controller.model.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.*
import ru.quipy.logic.person.PersonAggregateState
import ru.quipy.projections.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    val personService: EventSourcingService<UUID, PersonAggregate, PersonAggregateState>,
    val projectProjection: ProjectProjection,
    val statusProjection: StatusProjection,
    val taskProjection: TaskProjection,
) {

    @PostMapping
    fun createProject(@RequestBody request: CreateProjectRequest): ResponseEntity<ProjectCreatedResponse> {
        val projectCreatedEvent = projectService.create {
            it.createProject(
                title = request.title,
                personId = request.personCreatorId,
            )
        }

        val statusCreatedEvent = projectService.update(projectCreatedEvent.projectId) {
            it.createStatus()
        }

        val personAddedToProjectEvent = projectService.update(projectCreatedEvent.projectId) {
            val person = personService.getState(request.personCreatorId)

            it.addPerson(
                projectId = projectCreatedEvent.projectId,
                personId = person?.personId,
                username = person?.username,
                firstName = person?.firstName,
                middleName = person?.middleName,
                lastName = person?.lastName,
            )
        }

        return ResponseEntity.ok(
            ProjectCreatedResponse(
                projectCreatedEvent = projectCreatedEvent,
                statusCreatedEvent = statusCreatedEvent,
                personAddedToProjectEvent = personAddedToProjectEvent,
            )
        )
    }

    @PostMapping("/{projectId}/members")
    fun addMemberToProject(
        @PathVariable projectId: UUID,
        @RequestBody request: AddPersonToProjectRequest,
    ): ResponseEntity<PersonAddedToProjectEvent> {
        val memberCreatedEvent = projectService.update(projectId) {
            val person = personService.getState(request.personToAddId)

            it.addPerson(
                projectId = projectId,
                personId = person?.personId,
                username = person?.username,
                firstName = person?.firstName,
                middleName = person?.middleName,
                lastName = person?.lastName,
            )
        }

        return ResponseEntity.ok(memberCreatedEvent)
    }

    @PostMapping("/{projectId}/tasks")
    fun createTask(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskCreatedEvent> {
        val taskCreatedEvent = projectService.update(projectId) {
            it.createTask(
                projectId = projectId,
                title = request.title,
                statusId = request.statusId,
                assignees = request.assignees.toMutableList(),
            )
        }

        return ResponseEntity.ok(taskCreatedEvent)
    }

    @PostMapping("/{projectId}/{taskId}/assignees")
    fun assignMemberToTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: AddMemberToTaskRequest,
    ): ResponseEntity<PersonAssignedEvent> {
        val memberAssignedEvent = projectService.update(projectId) {
            it.assignPersonToTask(
                taskId = taskId,
                personId = request.memberId,
                projectId = projectId
            )
        }

        return ResponseEntity.ok(memberAssignedEvent)
    }

    @PostMapping("/{projectId}/{taskId}/status")
    fun updateTaskStatus(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskStatusRequest,
    ): ResponseEntity<TaskStatusUpdatedEvent> {
        val taskStatusUpdatedEvent = projectService.update(projectId) {
            it.updateTaskStatus(
                taskId = taskId,
                statusId = request.statusId,
                projectId = projectId
            )
        }

        return ResponseEntity.ok(taskStatusUpdatedEvent)
    }

    @PostMapping("/{projectId}/{taskId}/name")
    fun updateTaskName(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskNameRequest,
    ): ResponseEntity<TaskRenamedEvent> {
        val taskRenamedEvent = projectService.update(projectId) {
            it.updateTaskName(
                taskId = taskId,
                title = request.title,
                projectId = projectId
            )
        }

        return ResponseEntity.ok(taskRenamedEvent)
    }

    @PostMapping("/{projectId}/statuses")
    fun createStatus(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateStatusRequest,
    ): ResponseEntity<StatusCreatedResponse> {

        val statusCreatedEvent = projectService.update(projectId) {
            it.createStatus(
                name = request.name,
                color = request.color,
            )
        }

        val possibleStatusesUpdatedEvent = projectService.update(projectId) { projectAggregateState ->
            projectAggregateState.updateStatusOrder(
                orderedStatuses = projectAggregateState.statuses.map { it.id } + statusCreatedEvent.statusId,
                projectId = projectId,
            )
        }

        return ResponseEntity.ok(
            StatusCreatedResponse(
                statusCreatedEvent = statusCreatedEvent,
                possibleStatusesUpdatedEvent = possibleStatusesUpdatedEvent,
            )
        )
    }

    @PatchMapping("/{projectId}/statuses")
    fun updateStatusOrder(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateStatusOrderRequest,
    ): ResponseEntity<PossibleStatusesUpdatedEvent> {
        val possibleStatusesUpdatedEvent = projectService.update(projectId) {
            it.updateStatusOrder(
                orderedStatuses = request.orderedStatuses,
                projectId = projectId,
            )
        }

        return ResponseEntity.ok(possibleStatusesUpdatedEvent)
    }

    @DeleteMapping("/{projectId}/statuses/{statusId}")
    fun deleteStatus(
        @PathVariable projectId: UUID,
        @PathVariable statusId: UUID,
    ): ResponseEntity<StatusDeletedResponse> {

        val statusesUpdatedEvent = projectService.update(projectId) { projectState ->
            projectState.updateStatusOrder(
                orderedStatuses = projectState.statuses.map { it.id }.filter { it != statusId },
                projectId = projectId,
            )
        }

        val statusDeletedEvent = projectService.update(projectId) {
            it.deleteStatus(
                statusId = statusId,
                projectId = projectId,
            )
        }

        return ResponseEntity.ok(
            StatusDeletedResponse(
                statusDeletedEvent = statusDeletedEvent,
                possibleStatusesUpdatedEvent = statusesUpdatedEvent,
            )
        )
    }

    @GetMapping("/all") //done
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectProjection.getAllProjects().map { it.toDto() } )
    }

    @GetMapping("/users/{personId}") //done
    fun getProjectsByPersonId(
        @PathVariable personId: String
    ): ResponseEntity<List<ProjectDto>?> {
        val projects = projectProjection.getProjectsByPersonId(personId)
        return ResponseEntity.ok(projects.map { it.toDto() })
    }

    @GetMapping("/tasks/all") //done
    fun getAllTasks(): ResponseEntity<List<TaskDto>> {
        val tasks = taskProjection.getAllTasks()
        return ResponseEntity.ok(tasks.map { it.toDto() })
    }

    @GetMapping("/{projectId}/tasks") //done
    fun getTasksByProjectId(
        @PathVariable projectId: String
    ): ResponseEntity<List<TaskDto>> {
        val tasks = taskProjection.getTasksByProjectId(projectId)
        return ResponseEntity.ok(tasks.map { it.toDto() })
    }

    @GetMapping("/{projectId}/tasks/by_status") //done
    fun getTasksByStatusId(
        @PathVariable projectId: String,
        @RequestParam statusId: String
    ): ResponseEntity<List<TaskDto>> {
        return ResponseEntity.ok(
            taskProjection.getTasksByStatusId(statusId).map { it.toDto() }
        )
    }

    @GetMapping("/{projectId}/tasks/{taskId}")
    fun getTask(
        @PathVariable projectId: String,
        @PathVariable taskId: String
    ): ResponseEntity<TaskDto?> {
        return ResponseEntity.ok(
            taskProjection.getTaskById(taskId)?.toDto()
        )
    }

    @GetMapping("/{projectId}/tasks/{taskId}/status")
    fun getStatusByTaskId(
        @PathVariable projectId: String,
        @PathVariable taskId: String
    ): ResponseEntity<StatusDto?> {
        return ResponseEntity.ok(
            statusProjection.getStatusByTaskId(taskId)?.toDto()
        )
    }

    @GetMapping("/{projectId}/statuses")
    fun getStatuses(
        @PathVariable projectId: String
    ): ResponseEntity<List<StatusDto>> {
        return ResponseEntity.ok(
            statusProjection.getStatusesByProjectId(projectId)?.map { it.toDto() } ?: emptyList()
        )
    }

    @GetMapping("/{projectId}/members") //done
    fun getMembersByProjectID(
        @PathVariable projectId: String
    ): ResponseEntity<List<PersonResponse>> {
        val users = projectProjection.getPersonsByProjectId(projectId)
        return ResponseEntity.ok(users.map { it.toDto() })
    }
}
