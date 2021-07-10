package com.appifyhub.monolith.init

import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.admin.SignatureGenerator
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.schema.SchemaService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.Stubs
import com.nhaarman.mockitokotlin2.any
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

  private val adminService = mock<AdminService>()
  private val userService = mock<UserService>()
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

    verifyZeroInteractions(adminService)
    verifyZeroInteractions(userService)
    verify(schemaService, never()).update(any())
  }

  @Test fun `initial seed fails if project name is blank`() {
    assertThat { runInitializer(adminProjectName = " ") }
      .isFailure()
      .messageContains("Project Name")

    verifyZeroInteractions(adminService)
    verifyZeroInteractions(userService)
  }

  @Test fun `initial seed fails if owner name is blank`() {
    assertThat { runInitializer(adminOwnerName = " ") }
      .isFailure()
      .messageContains("Owner Name")

    verifyZeroInteractions(adminService)
    verifyZeroInteractions(userService)
  }

  @Test fun `initial seed fails if owner email is blank`() {
    assertThat { runInitializer(adminOwnerEmail = " ") }
      .isFailure()
      .messageContains("Owner Email")

    verifyZeroInteractions(adminService)
    verifyZeroInteractions(userService)
  }

  @Test fun `initial seed uses config signature if present`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val account = Stubs.account.copy(
      owners = Stubs.account.owners.map { it.copy(ownedTokens = emptyList()) },
    )
    val user = Stubs.user.copy(
      id = UserId(Stubs.user.contact!!, project.id),
      ownedTokens = emptyList(),
    )

    adminService.stub {
      on { addAccount() } doReturn account
      on { addProject(any()) } doReturn project
    }
    userService.stub {
      on { addUser(any(), any()) } doReturn user
      on { updateUser(any(), any()) } doReturn user
    }

    assertThat { runInitializer(adminOwnerSecret = "root secret") }
      .isSuccess()

    verify(adminService).addAccount()
    verify(adminService).addProject(
      ProjectCreator(
        account = account,
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
      )
    )
    verify(userService).addUser(
      userIdType = project.userIdType,
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
    verify(userService).updateUser(
      userIdType = project.userIdType,
      updater = UserUpdater(
        id = UserId(user.contact!!, project.id),
        verificationToken = Settable(null),
        account = Settable(account),
      )
    )
  }

  @Test fun `initial seed generates a new signature if configured signature is blank`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val account = Stubs.account.copy(
      owners = Stubs.account.owners.map { it.copy(ownedTokens = emptyList()) },
    )
    val user = Stubs.user.copy(
      id = UserId(Stubs.user.contact!!, project.id),
      ownedTokens = emptyList(),
    )

    adminService.stub {
      on { addAccount() } doReturn account
      on { addProject(any()) } doReturn project
    }
    userService.stub {
      on { addUser(any(), any()) } doReturn user
      on { updateUser(any(), any()) } doReturn user
    }

    SignatureGenerator.interceptor = { "generated sig" }

    assertThat { runInitializer(adminOwnerSecret = " \t\n ") }
      .isSuccess()

    verify(adminService).addAccount()
    verify(adminService).addProject(
      ProjectCreator(
        account = account,
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
      )
    )
    verify(userService).addUser(
      userIdType = project.userIdType,
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
    verify(userService).updateUser(
      userIdType = project.userIdType,
      updater = UserUpdater(
        id = UserId(user.contact!!, project.id),
        verificationToken = Settable(null),
        account = Settable(account),
      )
    )
  }

  // Helpers

  private fun runInitializer(
    adminOwnerName: String = Stubs.user.name!!,
    adminOwnerSecret: String = Stubs.userCreator.rawSecret,
    adminOwnerEmail: String = Stubs.user.contact!!,
    adminProjectName: String = "Project Name",
    args: ApplicationArguments? = null,
  ) = SchemaInitializer(
    adminService = adminService,
    userService = userService,
    schemaService = schemaService,
    adminConfig = AdminProjectConfig().apply {
      this.ownerName = adminOwnerName
      this.ownerSecret = adminOwnerSecret
      this.ownerEmail = adminOwnerEmail
      this.projectName = adminProjectName
    },
  ).run(args)

}
