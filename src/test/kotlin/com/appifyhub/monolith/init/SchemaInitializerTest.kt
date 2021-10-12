package com.appifyhub.monolith.init

import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreationInfo
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.creator.SignatureGenerator
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.creator.PropertyService
import com.appifyhub.monolith.service.schema.SchemaService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.Stubs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.ApplicationArguments

class SchemaInitializerTest {

  private val creatorService = mock<CreatorService>()
  private val userService = mock<UserService>()
  private val propService = mock<PropertyService>()
  private val schemaService = mock<SchemaService>()

  @BeforeEach fun setup() {
    schemaService.stub {
      on { update(any()) } doAnswer { }
      on { isInitialized(any()) } doReturn false
    }
  }

  @AfterEach fun teardown() {
    SignatureGenerator.interceptor = { null }
  }

  @Test fun `already initialized schema won't initialize again`() {
    schemaService.stub {
      on { isInitialized(any()) } doReturn true
    }

    runInitializer()

    verifyZeroInteractions(creatorService, userService, propService)
    verify(schemaService, never()).update(any())
  }

  @Test fun `initial seed fails if project name is blank`() {
    assertThat { runInitializer(creatorProjectName = " ") }
      .isFailure()
      .messageContains("Project Name")

    verifyZeroInteractions(creatorService, userService, propService)
  }

  @Test fun `initial seed fails if owner name is blank`() {
    assertThat { runInitializer(creatorOwnerName = " ") }
      .isFailure()
      .messageContains("Owner Name")

    verifyZeroInteractions(creatorService, userService, propService)
  }

  @Test fun `initial seed fails if owner email is blank`() {
    assertThat { runInitializer(creatorOwnerEmail = " ") }
      .isFailure()
      .messageContains("Owner Email")

    verifyZeroInteractions(creatorService, userService, propService)
  }

  @Test fun `initial seed uses config signature if present`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val user = Stubs.user.copy(id = UserId(Stubs.user.contact!!, project.id))

    creatorService.stub {
      on { addProject(any(), anyOrNull()) } doReturn project
    }
    userService.stub {
      on { addUser(any()) } doReturn user
      on { updateUser(any()) } doReturn user
    }

    assertThat { runInitializer(creatorOwnerSecret = "root secret") }
      .isSuccess()

    verify(creatorService).addProject(
      creationInfo = ProjectCreationInfo(
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
      ),
      creator = null,
    )
    verify(propService).saveProperty<String>(
      projectId = project.id,
      propName = "NAME",
      propRawValue = "Project Name",
    )
    verify(propService).saveProperty<String>(
      projectId = project.id,
      propName = "ON_HOLD",
      propRawValue = "false",
    )
    verify(userService) {
      mock.addUser(
        creator = UserCreator(
          userId = user.contact,
          projectId = project.id,
          rawSecret = "root secret",
          name = user.name,
          type = User.Type.ORGANIZATION,
          authority = User.Authority.OWNER,
          allowsSpam = true,
          contact = user.contact,
          contactType = User.ContactType.EMAIL,
          birthday = null,
          company = null,
        )
      )
      mock.updateUser(
        updater = UserUpdater(
          id = UserId(user.contact!!, project.id),
          verificationToken = Settable(null),
        )
      )
    }
  }

  @Test fun `initial seed generates a new signature if configured signature is blank`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val user = Stubs.user.copy(id = UserId(Stubs.user.contact!!, project.id))

    creatorService.stub {
      on { addProject(any(), anyOrNull()) } doReturn project
    }
    userService.stub {
      on { addUser(any()) } doReturn user
      on { updateUser(any()) } doReturn user
    }

    SignatureGenerator.interceptor = { "generated sig" }

    assertThat { runInitializer(creatorOwnerSecret = " \t\n ") }
      .isSuccess()

    verify(creatorService).addProject(
      creationInfo = ProjectCreationInfo(
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
      ),
      creator = null,
    )
    verify(propService).saveProperty<String>(
      projectId = project.id,
      propName = "NAME",
      propRawValue = "Project Name",
    )
    verify(propService).saveProperty<String>(
      projectId = project.id,
      propName = "ON_HOLD",
      propRawValue = "false",
    )
    verify(userService) {
      mock.addUser(
        creator = UserCreator(
          userId = user.contact,
          projectId = project.id,
          rawSecret = "generated sig",
          name = user.name,
          type = User.Type.ORGANIZATION,
          authority = User.Authority.OWNER,
          allowsSpam = true,
          contact = user.contact,
          contactType = User.ContactType.EMAIL,
          birthday = null,
          company = null,
        )
      )
      mock.updateUser(
        updater = UserUpdater(
          id = UserId(user.contact!!, project.id),
          verificationToken = Settable(null),
        )
      )
    }
  }

  // Helpers

  private fun runInitializer(
    creatorOwnerName: String = Stubs.user.name!!,
    creatorOwnerSecret: String = Stubs.userCreator.rawSecret,
    creatorOwnerEmail: String = Stubs.user.contact!!,
    creatorProjectName: String = "Project Name",
    args: ApplicationArguments? = null,
  ) = SchemaInitializer(
    creatorService = creatorService,
    userService = userService,
    propertyService = propService,
    schemaService = schemaService,
    creatorConfig = CreatorProjectConfig().apply {
      this.ownerName = creatorOwnerName
      this.ownerSecret = creatorOwnerSecret
      this.ownerEmail = creatorOwnerEmail
      this.projectName = creatorProjectName
    },
  ).run(args)

}
