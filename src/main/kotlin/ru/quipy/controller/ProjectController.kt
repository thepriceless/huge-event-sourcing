package ru.quipy.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.api.member.MemberAggregate
import ru.quipy.api.member.MemberCreatedEvent
import ru.quipy.api.project.ProjectAggregate
import ru.quipy.api.project.ProjectUpdatedEvent
import ru.quipy.api.taskstatus.TaskCreatedEvent
import ru.quipy.api.taskstatus.TaskStatusAggregate
import ru.quipy.controller.model.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.*
import ru.quipy.logic.taskstatus.TaskStatusAggregateState
import ru.quipy.logic.taskstatus.createStatus
import ru.quipy.logic.taskstatus.createTask
import ru.quipy.logic.taskstatus.deleteStatus
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    val taskStatusService: EventSourcingService<UUID, TaskStatusAggregate, TaskStatusAggregateState>,
    val memberService: EventSourcingService<UUID, MemberAggregate, MemberAggregateState>
) {

    @PostMapping
    fun createProject(@RequestBody request: CreateProjectRequest): ProjectCreatedResponse {
        val projectCreatedEvent = projectService.create {
            it.createProject(
                title = request.title,
                username = request.username,
            )
        }

        val statusCreatedEvent = taskStatusService.create {
            it.createStatus(
                projectId = projectCreatedEvent.projectId,
            )
        }

        val memberCreatedEvent = memberService.create {
            it.createMember(
                projectId = projectCreatedEvent.projectId,
                username = request.username,
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
    ): MemberCreatedEvent = memberService.create {
        it.addMemberToProject(
            projectId = projectId,
            username = request.username,
        )
    }

    @PostMapping("/{projectId}/tasks")
    fun createTask(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateTaskRequest,
    ): TaskCreatedEvent = taskStatusService.create {
        it.createTask(
            projectId = projectId,
            title = request.title,
            statusId = request.statusId,
            assignees = request.assignees,
        )
    }

    @PostMapping("/{projectId}/statuses")
    fun createStatus(
        @PathVariable projectId: UUID,
        @RequestBody request: CreateStatusRequest,
    ): StatusCreatedResponse {

        val projectUpdatedEvent = projectService.update(projectId) {
            it.createStatus(
                projectId = projectId,
                name = request.name,
                color = request.color,
            )
        }

        val statusCreatedEvent = taskStatusService.create {
            it.createStatus(
                projectId = projectId,
                name = request.name,
                color = request.color,
            )
        }

        return StatusCreatedResponse(
            projectUpdatedEvent = projectUpdatedEvent,
            statusCreatedEvent = statusCreatedEvent,
        )
    }

    @PatchMapping("/{projectId}/statuses")
    fun updateStatusOrder(
        @PathVariable projectId: UUID,
        @RequestBody request: UpdateStatusOrderRequest,
    ): ProjectUpdatedEvent = projectService.update(projectId) {
        it.updateStatusOrder(
            projectId = projectId,
            orderedStatuses = request.orderedStatuses,
        )
    }

    @DeleteMapping("/{projectId}/statuses/{statusId}")
    fun deleteStatus(
        @PathVariable projectId: UUID,
        @PathVariable statusId: UUID,
    ): StatusDeletedResponse {

        val statusDeletedEvent = taskStatusService.update(statusId) {
            it.deleteStatus(
                projectId = projectId,
                statusId = statusId,
            )
        }

        val projectUpdatedEvent = projectService.update(projectId) {
            it.deleteStatus(
                projectId = projectId,
                statusId = statusId,
            )
        }

        return StatusDeletedResponse(
            projectUpdatedEvent = projectUpdatedEvent,
            statusDeletedEvent = statusDeletedEvent,
        )
    }

    @GetMapping("/{projectId}")
    fun getAccount(@PathVariable projectId: UUID): ProjectAggregateState? {
        return projectService.getState(projectId)
    }
}
