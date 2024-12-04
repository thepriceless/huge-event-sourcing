package ru.quipy.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.quipy.api.PersonAggregate
import ru.quipy.api.PersonCreatedEvent
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserCreatedEvent
import ru.quipy.controller.model.CreatePersonRequest
import ru.quipy.controller.model.PersonCreatedResponse
import ru.quipy.controller.model.PersonResponse
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.person.PersonAggregateState
import ru.quipy.logic.user.UserAggregateState
import ru.quipy.logic.person.createPerson
import ru.quipy.logic.user.createUser
import ru.quipy.projections.PersonProjection
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    val personService: EventSourcingService<UUID, PersonAggregate, PersonAggregateState>,
    val userService: EventSourcingService<UUID, UserAggregate, UserAggregateState>,
    val personProjection: PersonProjection
) {

    @PostMapping("/signup")
    fun createUser(
        @RequestBody request: CreatePersonRequest
    ): ResponseEntity<PersonCreatedEvent> {
        val userCreatedEvent = userService.create {
            it.createUser(request.password)
        }

        val personCreatedEvent = personService.create {
            it.createPerson(
                username = request.username,
                firstName = request.firstName,
                middleName = request.middleName,
                lastName = request.lastName,
                userId = userCreatedEvent.userId,
            )
        }

        return ResponseEntity.ok(personCreatedEvent)
    }

    @GetMapping("/all")
    fun getAllPersons(): ResponseEntity<List<PersonResponse>> {
        val users = personProjection.getAllPersons()
        return ResponseEntity.ok(
            users.map {
                PersonResponse(
                    username = it.username,
                    firstName = it.firstName,
                    middleName = it.middleName,
                    lastName = it.lastName,
                )
            }
        )
    }

    @GetMapping("/get")
    fun getUserByID(
        @RequestParam("username") username: String
    ): ResponseEntity<PersonResponse> {
        val user = personProjection.getUser(username) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            PersonResponse(
                username = user.username,
                firstName = user.firstName,
                middleName = user.middleName,
                lastName = user.lastName,
            )
        )
    }
}
