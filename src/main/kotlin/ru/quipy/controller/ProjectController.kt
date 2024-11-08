package ru.quipy.controller

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
    fun createProject(@RequestBody request: CreateProjectRequest): ProjectCreatedResponse {
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

        return ProjectCreatedResponse(
            projectCreatedEvent = projectCreatedEvent,
            memberCreatedEvent = memberCreatedEvent,
            statusCreatedEvent = statusCreatedEvent,
        )
    }

    @PostMapping("/{projectId}/members")
    fun addMemberToProject(
        @PathVariable projectId: UUID,
        @RequestBody request: AddMemberToProjectRequest,
    ): MemberCreatedEvent = projectService.update(projectId) {
        val user = userService.getState(request.username)

        it.createMember(
            projectId = projectId,
            username = request.username,
            firstName = user?.firstName,
            middleName = user?.middleName,
            lastName = user?.lastName,
        )
    }

    @PostMapping("/{projectId}/tasks")
    fun createTask(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateTaskRequest,
    ): TaskCreatedEvent = projectService.update(projectId) {
        it.createTask(
            projectId = projectId,
            title = request.title,
            statusId = request.statusId,
            assignees = request.assignees.toMutableList(),
        )
    }

    @PostMapping("/{projectId}/{taskId}/assignees")
    fun assignMemberToTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: AddMemberToTaskRequest,
    ): MemberAssignedEvent = projectService.update(taskId) {
        it.assignMemberToTask(
            taskId = taskId,
            memberId = request.memberId,
        )
    }

    @PostMapping("/{projectId}/{taskId}/status")
    fun updateTaskStatus(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskStatusRequest,
    ): TaskStatusUpdatedEvent = projectService.update(taskId) {
        it.updateTaskStatus(
            taskId = taskId,
            statusId = request.statusId,
        )
    }

    @PostMapping("/{projectId}/{taskId}/name")
    fun updateTaskName(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskNameRequest,
    ): TaskRenamedEvent = projectService.update(projectId) {
        it.updateTaskName(
            taskId = taskId,
            title = request.title,
        )
    }

    @PostMapping("/{projectId}/statuses")
    fun createStatus(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateStatusRequest,
    ): StatusCreatedResponse {

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

        return StatusCreatedResponse(
            possibleStatusesUpdatedEvent = possibleStatusesUpdatedEvent,
            statusCreatedEvent = statusCreatedEvent,
        )
    }

    @PatchMapping("/{projectId}/statuses")
    fun updateStatusOrder(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateStatusOrderRequest,
    ): PossibleStatusesUpdatedEvent = projectService.update(projectId) {
        it.updateStatusOrder(
            orderedStatuses = request.orderedStatuses,
        )
    }

    @DeleteMapping("/{projectId}/statuses/{statusId}")
    fun deleteStatus(
        @PathVariable projectId: UUID,
        @PathVariable statusId: UUID,
    ): StatusDeletedResponse {

        val statusesUpdatedEvent = projectService.update(projectId) { projectState ->
            projectState.updateStatusOrder(
                orderedStatuses = projectState.statuses.map { it.id }.filter { it != statusId },
            )
        }

        val statusDeletedEvent = projectService.update(statusId) {
            it.deleteStatus(
                statusId = statusId,
            )
        }

        return StatusDeletedResponse(
            possibleStatusesUpdatedEvent = statusesUpdatedEvent,
            statusDeletedEvent = statusDeletedEvent,
        )
    }

    @GetMapping("/{projectId}")
    fun getAccount(@PathVariable projectId: UUID): ProjectAggregateState? {
        return projectService.getState(projectId)
    }

    @GetMapping("/{projectId}/{taskId}")
    fun getTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID
    ): TaskEntity? {
        return projectService.getState(projectId)?.tasks?.firstOrNull { it.id == taskId }
    }
}
