package com.appifyhub.monolith.features.init.domain

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.service.CreatorService
import com.appifyhub.monolith.features.creator.domain.service.MessageTemplateService
import com.appifyhub.monolith.features.creator.repository.SignatureGenerator
import com.appifyhub.monolith.features.init.domain.model.InitializationConfig
import com.appifyhub.monolith.features.init.domain.service.SchemaService
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.user.domain.service.UserService
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.boot.ApplicationArguments
import java.util.Locale

class SchemaInitializerTest {

  private val creatorService = mock<CreatorService>()
  private val userService = mock<UserService>()
  private val schemaService = mock<SchemaService>()
  private val messageTemplateService = mock<MessageTemplateService>()

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

    verifyNoMoreInteractions(creatorService, userService)
    verify(schemaService, never()).update(any())
  }

  @Test fun `initial seed fails if project name is blank`() {
    assertFailure { runInitializer(creatorProjectName = " ") }
      .messageContains("Project Name")

    verifyNoMoreInteractions(creatorService, userService)
  }

  @Test fun `initial seed fails if owner name is blank`() {
    assertFailure { runInitializer(superCreatorName = " ") }
      .messageContains("Owner Name")

    verifyNoMoreInteractions(creatorService, userService)
  }

  @Test fun `initial seed fails if owner email is blank`() {
    assertFailure { runInitializer(superCreatorEmail = " ") }
      .messageContains("Owner Email")

    verifyNoMoreInteractions(creatorService, userService)
  }

  @Test fun `initial seed uses config signature if present`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val user = Stubs.user.copy(id = UserId(Stubs.user.contact!!, project.id))

    creatorService.stub {
      on { addProject(any()) } doReturn project
    }
    userService.stub {
      on { addUser(any()) } doReturn user
      on { updateUser(any()) } doReturn user
    }

    assertThat(runInitializer(superCreatorSignature = "root secret"))
      .isEqualTo(Unit)

    verify(creatorService).addProject(
      projectData = ProjectCreator(
        owner = null,
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
        name = "Project Name",
        description = null,
        logoUrl = null,
        websiteUrl = null,
        maxUsers = CreatorService.DEFAULT_MAX_USERS,
        anyoneCanSearch = false,
        onHold = false,
        languageTag = Locale.US.toLanguageTag(),
        requiresSignupCodes = false,
        maxSignupCodesPerUser = Integer.MAX_VALUE,
        mailgunConfig = null,
        twilioConfig = null,
        firebaseConfig = null,
      ),
    )
    verify(userService) {
      mock.addUser(
        creator = UserCreator(
          userId = user.contact,
          projectId = project.id,
          rawSignature = "root secret",
          name = user.name,
          type = User.Type.ORGANIZATION,
          authority = User.Authority.OWNER,
          allowsSpam = true,
          contact = user.contact,
          contactType = User.ContactType.EMAIL,
          birthday = null,
          company = null,
          languageTag = Locale.US.toLanguageTag(),
          signupCode = null,
        ),
      )
      mock.updateUser(
        updater = UserUpdater(
          id = UserId(user.contact!!, project.id),
          verificationToken = Settable(null),
        ),
      )
    }
    verify(messageTemplateService).initializeDefaults()
  }

  @Test fun `initial seed generates a new signature if configured signature is blank`() {
    val project = Stubs.project.copy(
      userIdType = Project.UserIdType.EMAIL,
      type = Project.Type.FREE,
    )
    val user = Stubs.user.copy(id = UserId(Stubs.user.contact!!, project.id))

    creatorService.stub {
      on { addProject(any()) } doReturn project
    }
    userService.stub {
      on { addUser(any()) } doReturn user
      on { updateUser(any()) } doReturn user
    }

    SignatureGenerator.interceptor = { "generated sig" }

    assertThat(runInitializer(superCreatorSignature = " \t\n "))
      .isEqualTo(Unit)

    verify(creatorService).addProject(
      projectData = ProjectCreator(
        owner = null,
        type = project.type,
        status = project.status,
        userIdType = project.userIdType,
        name = "Project Name",
        description = null,
        logoUrl = null,
        websiteUrl = null,
        maxUsers = CreatorService.DEFAULT_MAX_USERS,
        anyoneCanSearch = false,
        onHold = false,
        languageTag = Locale.US.toLanguageTag(),
        requiresSignupCodes = false,
        maxSignupCodesPerUser = Integer.MAX_VALUE,
        mailgunConfig = null,
        twilioConfig = null,
        firebaseConfig = null,
      ),
    )

    verify(userService) {
      mock.addUser(
        creator = UserCreator(
          userId = user.contact,
          projectId = project.id,
          rawSignature = "generated sig",
          name = user.name,
          type = User.Type.ORGANIZATION,
          authority = User.Authority.OWNER,
          allowsSpam = true,
          contact = user.contact,
          contactType = User.ContactType.EMAIL,
          birthday = null,
          company = null,
          languageTag = Locale.US.toLanguageTag(),
          signupCode = null,
        ),
      )
      mock.updateUser(
        updater = UserUpdater(
          id = UserId(user.contact!!, project.id),
          verificationToken = Settable(null),
        ),
      )
    }
    verify(messageTemplateService).initializeDefaults()
  }

  // Helpers

  private fun runInitializer(
    superCreatorName: String = Stubs.user.name!!,
    superCreatorSignature: String = Stubs.userCreator.rawSignature,
    superCreatorEmail: String = Stubs.user.contact!!,
    creatorProjectName: String = "Project Name",
    args: ApplicationArguments? = null,
  ) = SchemaInitializer(
    creatorService = creatorService,
    userService = userService,
    schemaService = schemaService,
    messageTemplateService = messageTemplateService,
    initializationConfig = InitializationConfig().apply {
      this.ownerName = superCreatorName
      this.ownerSignature = superCreatorSignature
      this.ownerEmail = superCreatorEmail
      this.projectName = creatorProjectName
    },
  ).run(args)

}
