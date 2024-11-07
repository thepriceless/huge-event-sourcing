package ru.quipy

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.quipy.api.project.PROJECT_CREATED_EVENT
import ru.quipy.controller.model.AddMemberToProjectRequest
import ru.quipy.controller.model.CreateProjectRequest
import ru.quipy.controller.model.CreateTaskRequest

import ru.quipy.controller.model.CreateUserRequest
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_COLOR
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_NAME
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProjectTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `project can be created`() {
        val createProjectRequest = CreateProjectRequest(
            title = "New Project",
            username = user1.username
        )

        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProjectRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.projectCreatedEvent.title").value("New Project"))
            .andExpect(jsonPath("$.projectCreatedEvent.name").value(PROJECT_CREATED_EVENT))
            .andExpect(jsonPath("$.projectCreatedEvent.version").value(1))

            .andExpect(jsonPath("$.memberCreatedEvent.username").value("vamos"))
            .andExpect(jsonPath("$.memberCreatedEvent.firstName").value("Kylian"))
            .andExpect(jsonPath("$.memberCreatedEvent.middleName").value("Mbappe"))
            .andExpect(jsonPath("$.memberCreatedEvent.lastName").value("Petrovich"))
            .andExpect(jsonPath("$.memberCreatedEvent.projectId").isNotEmpty)

            .andExpect(jsonPath("$.statusCreatedEvent.color").value(DEFAULT_STATUS_COLOR))
            .andExpect(jsonPath("$.statusCreatedEvent.statusName").value(DEFAULT_STATUS_NAME))
    }

    @Test
    @DisplayName("""
        1. Создаем проекта от лица юзера 1
        2. Добавляем в проект участником юзера 2
        3. Добавляем задачу в проект, назначая её на пользователя 2
    """)
    fun `task can be added to project`() {
        // Создаём проекта от лица юзера 1
        val createProjectRequest = CreateProjectRequest(
            title = "Project nice",
            username = user1.username
        )

        val projectResponse = mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProjectRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // Получаем id созданного проекта и id созданного статуса из ответа
        val projectId = objectMapper
            .readTree(projectResponse.response.contentAsString)["projectCreatedEvent"]["projectId"].asText()
        val statusId = objectMapper
            .readTree(projectResponse.response.contentAsString)["statusCreatedEvent"]["statusId"].asText()

        // Добавляем в проект участником юзера 2
        val createMemberRequest = AddMemberToProjectRequest(username = user2.username)

        val addMemberToProjectResponse = mockMvc.perform(
            post("/projects/$projectId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMemberRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value(user2.username))
            .andExpect(jsonPath("$.firstName").value(user2.firstName))
            .andExpect(jsonPath("$.middleName").value(user2.middleName))
            .andExpect(jsonPath("$.lastName").value(user2.lastName))
            .andExpect(jsonPath("$.projectId").value(projectId))
            .andReturn()

        // Добавляем задачу в проект, назначая её на пользователя 2

        val assigneeId = objectMapper
            .readTree(addMemberToProjectResponse.response.contentAsString)["memberId"].asText()

        val createTaskRequest = CreateTaskRequest(
            projectId = UUID.fromString(projectId),
            title = "New Task",
            statusId = UUID.fromString(statusId),
            assignees = listOf(UUID.fromString(assigneeId))
        )

        mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("New Task"))
            .andExpect(jsonPath("$.projectId").value(projectId))
            .andExpect(jsonPath("$.statusId").value(statusId))
            .andExpect(jsonPath("$.assignees").isArray)
            .andExpect(jsonPath("$.assignees[0]").value(assigneeId))
    }

    companion object {

        val user1 = CreateUserRequest(
            username = "vamos",
            firstName = "Kylian",
            middleName = "Mbappe",
            lastName = "Petrovich",
            password = "test",
        )

        val user2 = CreateUserRequest(
            username = "paco",
            firstName = "Yuri",
            middleName = "Zhirkov",
            lastName = "Valentinovich",
            password = "test2",
        )

        @BeforeAll
        @JvmStatic
        fun createUsers(
            @Autowired mockMvc: MockMvc,
            @Autowired objectMapper: ObjectMapper
        ) {
            // create user 1
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(user1)
                    )
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("vamos"))
                .andExpect(jsonPath("$.firstName").value("Kylian"))
                .andExpect(jsonPath("$.middleName").value("Mbappe"))
                .andExpect(jsonPath("$.lastName").value("Petrovich"))

            // create user 2
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(user2)
                    )
            )
        }
    }
}
