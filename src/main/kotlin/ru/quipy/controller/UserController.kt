package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserCreatedEvent
import ru.quipy.controller.model.CreateUserRequest
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.project.UserAggregateState
import ru.quipy.logic.user.createUser
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    val userService: EventSourcingService<String, UserAggregate, UserAggregateState>
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
}
