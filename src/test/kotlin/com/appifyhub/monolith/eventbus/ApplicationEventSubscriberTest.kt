package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.integrations.CommunicationsService
import com.appifyhub.monolith.service.messaging.MessageTemplateDefaults
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class ApplicationEventSubscriberTest {

  private val commsService = mock<CommunicationsService>()
  private val creatorService = mock<CreatorService>()

  private val ownerId = Stubs.userId.copy(userId = Stubs.userId.userId + 1)
  private val owner = Stubs.user.copy(id = ownerId)
  private val parentProjectId = Stubs.project.id + 1
  private val parentProject = Stubs.project.copy(id = parentProjectId)

  private val subscriber = ApplicationEventSubscriber(commsService, creatorService)

  @BeforeEach fun setup() {
    creatorService.stub {
      on { fetchProjectCreator(any()) } doReturn owner
    }
  }

  @Test fun `project created event sends a message`() {
    val event = ProjectCreated(
      ownerProject = parentProject,
      payload = Stubs.project,
    )

    subscriber.onEvent(event)

    verify(commsService).sendTo(
      projectId = parentProject.id,
      userId = owner.id,
      templateName = MessageTemplateDefaults.ProjectCreated.NAME,
      type = CommunicationsService.Type.EMAIL,
    )
  }

  @Test fun `user created event sends a message`() {
    val event = UserCreated(
      ownerProject = parentProject,
      payload = Stubs.user,
    )

    subscriber.onEvent(event)

    verify(commsService).sendTo(
      projectId = parentProject.id,
      userId = event.payload.id,
      templateName = MessageTemplateDefaults.UserCreated.NAME,
      type = CommunicationsService.Type.EMAIL,
    )
  }

  @Test fun `user auth reset completed event sends a message`() {
    val event = UserAuthResetCompleted(
      ownerProject = parentProject,
      payload = Stubs.user,
    )

    subscriber.onEvent(event)

    verify(commsService).sendTo(
      projectId = parentProject.id,
      userId = event.payload.id,
      overrideUser = event.payload,
      templateName = MessageTemplateDefaults.UserAuthResetCompleted.NAME,
      type = CommunicationsService.Type.EMAIL,
    )
  }

}
