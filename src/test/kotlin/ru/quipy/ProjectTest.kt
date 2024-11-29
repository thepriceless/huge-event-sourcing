package ru.quipy

import com.fasterxml.jackson.core.type.TypeReference
import org.hamcrest.Matchers.equalToIgnoringCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.quipy.api.PROJECT_CREATED_EVENT
import ru.quipy.controller.model.*
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_COLOR
import ru.quipy.logic.project.ProjectAggregateState.Companion.DEFAULT_STATUS_NAME
import ru.quipy.logic.project.TaskDto
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProjectTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `member can be added to project`() {
        val createMemberRequest = AddMemberToProjectRequest(username = user2.username)

        mockMvc.perform(
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
            .andReturn()
    }

    @Test
    @DisplayName("В созданный проект добавляем новую задачу")
    fun `task can be added to project`() {
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "New Task",
            statusId = statusId,
        )

        mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("New Task"))
            .andExpect(jsonPath("$.statusId").value(equalToIgnoringCase(statusId.toString().trim())))
            .andExpect(jsonPath("$.assignees").isEmpty)
            .andReturn()
    }

    @Test
    fun `member can be assigned to task`() {
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "Taskaaa",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        // Добавляем юзера 3 в проект
        val createMemberRequest = AddMemberToProjectRequest(username = user3.username)

        val addMember3ToProjectResponse = mockMvc.perform(
            post("/projects/$projectId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMemberRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value(user3.username))
            .andExpect(jsonPath("$.firstName").value(user3.firstName))
            .andExpect(jsonPath("$.middleName").value(user3.middleName))
            .andExpect(jsonPath("$.lastName").value(user3.lastName))
            .andReturn()

        val memberId3 = objectMapper
            .readTree(addMember3ToProjectResponse.response.contentAsString)["memberId"].asText()

        // Назначаем третьего участника на созданную задачу исполнителем

        val addMemberToTaskRequest = AddMemberToTaskRequest(memberId = UUID.fromString(memberId3))

        mockMvc.perform(
            post("/projects/$projectId/$taskId/assignees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addMemberToTaskRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.memberId").value(memberId3))
            .andReturn()
    }

    @Test
    @DisplayName("В созданный проект добавляем новый статус для задач")
    fun `create new status in project`() {
        val createStatusRequest = CreateStatusRequest(
            name = "New Status",
            color = "#123456"
        )

        mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusCreatedEvent.statusName").value("New Status"))
            .andExpect(jsonPath("$.statusCreatedEvent.color").value("#123456"))

            .andExpect(jsonPath("$.possibleStatusesUpdatedEvent.statuses").isArray)
            .andReturn()
    }

    @Test
    @DisplayName("В созданный проект добавляем новую задачу, новый статус, и меняем статус задачи на новый")
    fun `update status task`() {
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "SUPER TASK",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        val createStatusRequest = CreateStatusRequest(
            name = "STATUS VAMOS",
            color = "#295233"
        )

        val statusCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        ).andReturn()

        val newStatusId = objectMapper
            .readTree(statusCreatedEvent.response.contentAsString)["statusCreatedEvent"]["statusId"].asText()

        val updateTaskStatusRequest = UpdateTaskStatusRequest(
            statusId = UUID.fromString(newStatusId)
        )

        mockMvc.perform(
            post("/projects/$projectId/$taskId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskStatusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.taskId").value(taskId))
            .andExpect(jsonPath("$.statusId").value(newStatusId))
            .andReturn()
    }

    @Test
    @DisplayName("Создать задачу в проекте и изменить ее название")
    fun `update task name`() {
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "SUPER TASK",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        val updateTaskNameRequest = UpdateTaskNameRequest(
            title = "NEW TASK NAME"
        )

        mockMvc.perform(
            post("/projects/$projectId/$taskId/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskNameRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.taskId").value(taskId))
            .andExpect(jsonPath("$.title").value("NEW TASK NAME"))
            .andReturn()
    }

    @Test
    @DisplayName("Создать статус в проекте и удалить его")
    fun `delete status`() {
        val createStatusRequest = CreateStatusRequest(
            name = "New Status",
            color = "#123456"
        )

        val statusCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        ).andReturn()

        val statusId = objectMapper
            .readTree(statusCreatedEvent.response.contentAsString)["statusCreatedEvent"]["statusId"].asText()

        mockMvc.perform(
            delete("/projects/$projectId/statuses/$statusId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusDeletedEvent.statusId").value(statusId))
            .andReturn()
    }

    @Test
    @DisplayName("""
        1. Создать в проекте задачу
        2. Создать статус в проекте
        3. Присвоить задаче новый статус
        4. Попробовать удалить новый статус - поймать 400
    """)
    fun `delete status with assigned tasks`() {
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "SUPER TASK NEW",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        val createStatusRequest = CreateStatusRequest(
            name = "STATUS VAMOS RUSSIA",
            color = "#295233"
        )

        val statusCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        ).andReturn()

        val newStatusId = objectMapper
            .readTree(statusCreatedEvent.response.contentAsString)["statusCreatedEvent"]["statusId"].asText()

        val updateTaskStatusRequest = UpdateTaskStatusRequest(
            statusId = UUID.fromString(newStatusId)
        )

        mockMvc.perform(
            post("/projects/$projectId/$taskId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskStatusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.taskId").value(taskId))
            .andExpect(jsonPath("$.statusId").value(newStatusId))
            .andReturn()

        mockMvc.perform(
            delete("/projects/$projectId/statuses/$newStatusId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `two users with same username are prohibited`() {
        val userWithSameName = CreateUserRequest(
            username = user1.username,
            firstName = "asd",
            middleName = "Mbaasfasppe",
            lastName = "asdas",
            password = "test",
        )

        mockMvc.perform(
            post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(userWithSameName)
                )
        ).andExpect(status().isBadRequest)
    }

  /*  @Test
    fun `status order when delete one of them`() {
        //добавим два статуса
        var createStatusRequest = CreateStatusRequest(
            name = "STATUS VAMOS RUSSIA",
            color = "#295233"
        )

        val statusCreatedEvent1 = mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        ).andReturn()

        createStatusRequest = CreateStatusRequest(
            name = "lol",
            color = "#225"
        )

        val statusCreatedEvent2 = mockMvc.perform(
            post("/projects/$projectId/statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStatusRequest))
        ).andReturn()

        val statusId1 = UUID.fromString(objectMapper
            .readTree(statusCreatedEvent1.response.contentAsString)["statusCreatedEvent"]["statusId"].asText())

        val statusId2 = UUID.fromString(objectMapper
            .readTree(statusCreatedEvent2.response.contentAsString)["statusCreatedEvent"]["statusId"].asText())

        //удалим статус 0
        mockMvc.perform(
            delete("/projects/$projectId/statuses/$statusId")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)

        //проверим порядок оставшихся двух и что их 2
        val projectResponse = mockMvc.perform(
            get("/{projectId}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.statuses.length()").value(2))
            .andExpect(jsonPath("$.statuses[1].id").value(
                statusId1.toString()
            ))
            .andExpect(jsonPath("$.statuses[2].id").value(
                statusId2.toString()
            )).andReturn()
    }*/

    @Test
    fun `cant add non existent member to project`() {
        mockMvc.perform(
            post("/projects/$projectId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddMemberToProjectRequest("non exist user name")))
        )
            .andExpect(status().isBadRequest)
    }


    @Test
    fun `cant add task to non existent project`() {
        val fakeProjectId = UUID.randomUUID()
        val createTaskRequest = CreateTaskRequest(
            projectId = fakeProjectId,
            title = "New Task",
            statusId = statusId,
        )

        mockMvc.perform(
            post("/projects/$fakeProjectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `cant update task status with non existent statusId`() {
        //создаём задачу
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "New Task",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val updateTaskStatusRequest = UpdateTaskStatusRequest(
            statusId = UUID.randomUUID()
        )

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        mockMvc.perform(
            post("/projects/$projectId/$taskId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskStatusRequest))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `cant assign non existent member to task`() {
        //создаём задачу
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "New Task",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        mockMvc.perform(
            post("/projects/$projectId/$taskId/assignees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddMemberToTaskRequest(UUID.randomUUID())))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `x2 assign member to task`() {
        //создаём задачу
        val createTaskRequest = CreateTaskRequest(
            projectId = projectId,
            title = "New Task2",
            statusId = statusId,
        )

        val taskCreatedEvent = mockMvc.perform(
            post("/projects/$projectId/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest))

        ).andReturn()

        val taskId = objectMapper
            .readTree(taskCreatedEvent.response.contentAsString)["taskId"].asText()

        //добавляем мембера
        val createMemberRequest = AddMemberToProjectRequest(username = user2.username)

        val memberCreated = mockMvc.perform(
            post("/projects/$projectId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMemberRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val member2Id = UUID.fromString(objectMapper
            .readTree(memberCreated.response.contentAsString)["memberId"].asText())

        //назначаем его на задачу дважды
        mockMvc.perform(
            post("/projects/$projectId/$taskId/assignees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddMemberToTaskRequest(member2Id)))
        )
            .andExpect(status().isOk)

        // второй раз получаем 400
        mockMvc.perform(
            post("/projects/$projectId/$taskId/assignees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddMemberToTaskRequest(member2Id)))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `can get user`() {
        mockMvc.perform(
            get("/users/get?username=${user1.username}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(user1.username))
            .andExpect(jsonPath("$.firstName").value(user1.firstName))
            .andExpect(jsonPath("$.middleName").value(user1.middleName))
            .andExpect(jsonPath("$.lastName").value(user1.lastName))
    }

    @Test
    fun `cant get non existent user`() {
        mockMvc.perform(
            get("/users/get?username=non_existent_username")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get all users`() {
        mockMvc.perform(
            get("/users/all")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `can get project`() {
        mockMvc.perform(
            get("/projects/$projectId")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(projectId.toString()))
    }

    @Test
    fun `cant get non existent project` () {
        mockMvc.perform(
            get("/projects/${UUID.randomUUID()}")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(Int.MAX_VALUE)
    fun `get all projects` () {
        mockMvc.perform(
            get("/projects/all")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `get project by user` () {
        mockMvc.perform(
            get("/projects/users/${user1.username}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(projectId.toString()))
    }

    @Test
    fun `get project by user not found` () {
        mockMvc.perform(
            get("/projects/users/non_existent_user")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(Int.MAX_VALUE-1)
    fun `get tasks by project` () {
        val projectCreatedResponse = mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateProjectRequest(
                    title = "Get tasks project",
                    username = user1.username
                )))
        ).andReturn()

        val id = UUID.fromString(
            objectMapper
                .readTree(projectCreatedResponse.response.contentAsString)["projectCreatedEvent"]["projectId"].asText()
        )

        Thread.sleep(4000)

        mockMvc.perform(
            get("/projects/$id/tasks")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0))

        mockMvc.perform(
            post("/projects/$id/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateTaskRequest(
                    title = "Get tasks task",
                    statusId = statusId,
                    assignees = listOf(UUID.fromString(user1.username)),
                    projectId = id
                ))
            )
        )

        Thread.sleep(3000)

        mockMvc.perform(
            get("/projects/$id/tasks")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `get tasks by project not found` () {
        mockMvc.perform(
            get("/projects/fake_project/tasks")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get all tasks` () {
        mockMvc.perform(
            get("/projects/tasks/all")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
    }

    @Test
    @Order(Int.MAX_VALUE-2)
    fun `get tasks by status id`() {
        Thread.sleep(3000)
        val statuses = mockMvc.perform(
            get("/projects/${projectId}/tasks/by_status?statusId=${statusId}")
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        val tasks: List<TaskDto> = objectMapper.readValue(statuses, object : TypeReference<List<TaskDto>>() {})
        println(tasks.size)
        assert(tasks.size == 8)
    }

    @Test
    fun `get users by project` () {
        Thread.sleep(3000)
        mockMvc.perform(
            get("/projects/$projectId/users")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
    }

    companion object {

        val user1 = CreateUserRequest(
            username = UUID.randomUUID().toString(),
            firstName = "Kylian",
            middleName = "Mbappe",
            lastName = "Petrovich",
            password = "test",
        )

        val user2 = CreateUserRequest(
            username = UUID.randomUUID().toString(),
            firstName = "Yuri",
            middleName = "Zhirkov",
            lastName = "Valentinovich",
            password = "test2",
        )

        val user3 = CreateUserRequest(
            username = UUID.randomUUID().toString(),
            firstName = "3",
            middleName = "3",
            lastName = "3",
            password = "ewrwiehf",
        )

        lateinit var projectId: UUID
        lateinit var statusId: UUID

        @BeforeAll
        @JvmStatic
        fun setup(
            @Autowired mockMvc: MockMvc,
            @Autowired objectMapper: ObjectMapper
        ) {
            Thread.sleep(5000)

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
                .andExpect(jsonPath("$.username").value(user1.username))
                .andExpect(jsonPath("$.firstName").value(user1.firstName))
                .andExpect(jsonPath("$.middleName").value(user1.middleName))
                .andExpect(jsonPath("$.lastName").value(user1.lastName))

            // create user 2
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(user2)
                    )
            )

            // create user 3
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(user3)
                    )
            )

            // Создание проекта
            val createProjectRequest = CreateProjectRequest(
                title = "New Project",
                username = user1.username
            )

            val projectCreatedResponse = mockMvc.perform(
                post("/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createProjectRequest))
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectCreatedEvent.title").value("New Project"))
                .andExpect(jsonPath("$.projectCreatedEvent.name").value(PROJECT_CREATED_EVENT))
                .andExpect(jsonPath("$.projectCreatedEvent.version").value(1))

                .andExpect(jsonPath("$.memberCreatedEvent.username").value(user1.username))
                .andExpect(jsonPath("$.memberCreatedEvent.firstName").value(user1.firstName))
                .andExpect(jsonPath("$.memberCreatedEvent.middleName").value(user1.middleName))
                .andExpect(jsonPath("$.memberCreatedEvent.lastName").value(user1.lastName))
                .andExpect(jsonPath("$.memberCreatedEvent.projectId").isNotEmpty)

                .andExpect(jsonPath("$.statusCreatedEvent.color").value(DEFAULT_STATUS_COLOR))
                .andExpect(jsonPath("$.statusCreatedEvent.statusName").value(DEFAULT_STATUS_NAME))
                .andReturn()

            projectId = UUID.fromString(
                objectMapper
                    .readTree(projectCreatedResponse.response.contentAsString)["projectCreatedEvent"]["projectId"].asText()
            )

            statusId = UUID.fromString(
                objectMapper
                    .readTree(projectCreatedResponse.response.contentAsString)["statusCreatedEvent"]["statusId"].asText()
            )
        }
    }
}
