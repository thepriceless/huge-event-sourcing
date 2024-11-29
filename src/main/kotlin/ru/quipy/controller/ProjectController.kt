package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.quipy.api.*
import ru.quipy.api.UserAggregate
import ru.quipy.controller.model.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.*
import ru.quipy.projections.ProjectProjection
import ru.quipy.projections.UserProjectProjection
import ru.quipy.projections.toDto
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    val userService: EventSourcingService<String, UserAggregate, UserAggregateState>,
    val userProjectProjection: UserProjectProjection,
    val projectProjection: ProjectProjection
) {

    @PostMapping
    fun createProject(@RequestBody request: CreateProjectRequest): ResponseEntity<ProjectCreatedResponse> {
        val projectCreatedEvent = projectService.create {
            it.createProject(
                title = request.title,
                username = request.username,
            )
        }

        val statusCreatedEvent = projectService.update(projectCreatedEvent.projectId) {
            it.createStatus()
        }

        val memberCreatedEvent = projectService.update(projectCreatedEvent.projectId) {
            val user = userService.getState(request.username)

            it.createMember(
                projectId = projectCreatedEvent.projectId,
                username = request.username,
                firstName = user?.firstName,
                middleName = user?.middleName,
                lastName = user?.lastName,
            )
        }

        return ResponseEntity.ok(
            ProjectCreatedResponse(
                projectCreatedEvent = projectCreatedEvent,
                statusCreatedEvent = statusCreatedEvent,
                memberCreatedEvent = memberCreatedEvent,
            )
        )
    }

    @PostMapping("/{projectId}/members")
    fun addMemberToProject(
        @PathVariable projectId: UUID,
        @RequestBody request: AddMemberToProjectRequest,
    ): ResponseEntity<MemberCreatedEvent> {
        val memberCreatedEvent = projectService.update(projectId) {
            val user = userService.getState(request.username)

            it.createMember(
                projectId = projectId,
                username = request.username,
                firstName = user?.firstName,
                middleName = user?.middleName,
                lastName = user?.lastName,
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
    ): ResponseEntity<MemberAssignedEvent> {
        val memberAssignedEvent = projectService.update(projectId) {
            it.assignMemberToTask(
                taskId = taskId,
                memberId = request.memberId,
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

    @GetMapping("/{projectId}") //done
    fun getProject(@PathVariable projectId: String): ResponseEntity<ProjectDto> {
        val project = projectProjection.getProjectById(projectId)?.toDto() ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(project)
    }

    @GetMapping("/users/{username}") //done
    fun getProjectByUsername(
        @PathVariable username: String
    ): ResponseEntity<ProjectDto?> {
        val project = projectProjection.getProjectByUsername(username)?.toDto() ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(project)
    }

    @GetMapping("/tasks/all") //done
    fun getAllTasks(): ResponseEntity<List<TaskDto>> {
        val tasks = projectProjection.getAllTasks()
        return ResponseEntity.ok(tasks.map { it.toDto() })
    }

    @GetMapping("/{projectId}/tasks") //done
    fun getTasks(
        @PathVariable projectId: String
    ): ResponseEntity<List<TaskDto>> {
        val project = projectProjection.getProjectById(projectId)?.toDto() ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            projectProjection.getTaskByProjectId(projectId)?.map { it.toDto() } ?: emptyList()
        )
    }

    @GetMapping("/{projectId}/tasks/by_status") //done
    fun getTasksByStatusId(
        @PathVariable projectId: String,
        @RequestParam statusId: String
    ): ResponseEntity<List<TaskDto>> {
        return ResponseEntity.ok(
            projectProjection.getTasksByStatusId(statusId).map { it.toDto() }
        )
    }

    @GetMapping("/{projectId}/tasks/{taskId}")
    fun getTask(
        @PathVariable projectId: String,
        @PathVariable taskId: String
    ): ResponseEntity<TaskDto?> {
        return ResponseEntity.ok(
            projectProjection.getTaskById(taskId)?.toDto()
        )
    }

    @GetMapping("/{projectId}/tasks/{taskId}/status")
    fun getStatusByTaskId(
        @PathVariable projectId: String,
        @PathVariable taskId: String
    ): ResponseEntity<StatusDto?> {
        return ResponseEntity.ok(
            projectProjection.getStatusByTaskId(taskId)?.toDto()
        )
    }

    @GetMapping("/{projectId}/statuses")
    fun getStatuses(
        @PathVariable projectId: String
    ): ResponseEntity<List<StatusDto>> {
        return ResponseEntity.ok(
            projectProjection.getStatusesByProjectId(projectId)?.map { it.toDto() } ?: emptyList()
        )
    }

    @GetMapping("/{projectId}/users") //done
    fun getUsers(
        @PathVariable projectId: String
    ): ResponseEntity<List<UserResponse>> {
        val users = userProjectProjection.getProjectUsers(projectId).map {
            UserResponse(
                username = it.username,
                firstName = it.firstName,
                middleName = it.middleName,
                lastName = it.lastName,
            )
        }
        return ResponseEntity.ok(users)
    }
}
