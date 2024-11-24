package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserCreatedEvent
import ru.quipy.controller.model.CreateUserRequest
import ru.quipy.controller.model.UserResponse
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.UserAggregateState
import ru.quipy.logic.user.createUser
import ru.quipy.projections.UserProjection

@RestController
@RequestMapping("/users")
class UserController(
    val userService: EventSourcingService<String, UserAggregate, UserAggregateState>,
    val userProjection: UserProjection
) {

    @PostMapping("/signup")
    fun createUser(
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserCreatedEvent> {
        val existingUser = userService.getState(request.username)

        val userCreatedEvent = userService.create {
            it.createUser(
                username = request.username,
                firstName = request.firstName,
                middleName = request.middleName,
                lastName = request.lastName,
                password = request.password,
                existingUsername = existingUser?.username
            )
        }

        return ResponseEntity.ok(userCreatedEvent)
    }

    @GetMapping("/{username}")
    fun getUserByID(
        @PathVariable username: String
    ): ResponseEntity<UserResponse> {
        val user = userProjection.getUser(username) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            UserResponse(
                username = user.username,
                firstName = user.firstName,
                middleName = user.middleName,
                lastName = user.lastName,
            )
        )
    }

}
