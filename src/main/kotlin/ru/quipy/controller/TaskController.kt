package ru.quipy.controller

import org.springframework.web.bind.annotation.*
import ru.quipy.api.member.MemberAggregate
import ru.quipy.api.project.ProjectAggregate
import ru.quipy.api.taskstatus.TaskCreatedEvent
import ru.quipy.api.taskstatus.TaskStatusAggregate
import ru.quipy.api.taskstatus.TaskUpdatedEvent
import ru.quipy.controller.model.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.MemberAggregateState
import ru.quipy.logic.project.ProjectAggregateState
import ru.quipy.logic.taskstatus.*
import java.util.*

@RestController
@RequestMapping("/tasks")
class TaskController(
    val projectService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>,
    val taskStatusService: EventSourcingService<UUID, TaskStatusAggregate, TaskStatusAggregateState>,
    val memberService: EventSourcingService<UUID, MemberAggregate, MemberAggregateState>
) {

    @PostMapping
    fun createTask(
        @RequestBody request: CreateTaskRequest,
    ): TaskCreatedEvent = taskStatusService.create {
        it.createTask(
            projectId = request.projectId,
            title = request.title,
            statusId = request.statusId,
            assignees = request.assignees,
        )
    }

    @PutMapping("/{taskId}/assignees")
    fun assignMemberToTask(
        @PathVariable taskId: UUID,
        @RequestBody request: AddMemberToTaskRequest,
    ): TaskUpdatedEvent = taskStatusService.update(taskId) {
        it.assignMemberToTask(
            taskId = taskId,
            memberId = request.memberId,
        )
    }

    @PatchMapping("/{taskId}/status")
    fun updateTaskStatus(
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskStatusRequest,
    ): TaskUpdatedEvent = taskStatusService.update(taskId) {
        it.updateTaskStatus(
            taskId = taskId,
            statusId = request.statusId,
        )
    }

    @PatchMapping("/{taskId}/name")
    fun updateTaskName(
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskNameRequest,
    ): TaskUpdatedEvent = taskStatusService.update(taskId) {
        it.updateTaskName(
            taskId = taskId,
            title = request.title,
        )
    }
}
