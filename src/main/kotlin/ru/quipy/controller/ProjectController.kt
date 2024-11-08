package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.quipy.api.*
import ru.quipy.api.UserAggregate
import ru.quipy.controller.model.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    val userService: EventSourcingService<String, UserAggregate, UserAggregateState>,
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
            )
        }

        val statusDeletedEvent = projectService.update(projectId) {
            it.deleteStatus(
                statusId = statusId,
            )
        }

        return ResponseEntity.ok(
            StatusDeletedResponse(
                statusDeletedEvent = statusDeletedEvent,
                possibleStatusesUpdatedEvent = statusesUpdatedEvent,
            )
        )
    }

    @GetMapping("/{projectId}")
    fun getAccount(@PathVariable projectId: UUID): ResponseEntity<ProjectAggregateState?> {
        return ResponseEntity.ok(projectService.getState(projectId))
    }

    @GetMapping("/{projectId}/{taskId}")
    fun getTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<TaskEntity?> {
        return ResponseEntity.ok(
            projectService.getState(projectId)?.tasks?.firstOrNull { it.id == taskId }
        )
    }
}
