package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.features.creator.domain.service.CreatorService
import com.appifyhub.monolith.features.creator.domain.service.CommunicationsService
import com.appifyhub.monolith.features.creator.domain.service.MessageTemplateDefaults
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventSubscriber(
  private val commsService: CommunicationsService,
  private val creatorService: CreatorService,
) : EventSubscriber {

  private val log = LoggerFactory.getLogger(this::class.java)
  private val commsChannel = CommunicationsService.Type.EMAIL // just an opinionated choice

  @EventListener
  override fun onEvent(event: ApplicationEvent<*>) = try {
    when (event) {
      is ProjectCreated -> handleProjectCreated(event)
      is UserCreated -> handleUserCreated(event)
      is UserAuthResetCompleted -> handleUserAuthResetCompleted(event)
    }
  } catch (e: Exception) {
    log.warn("Failed to handle event: $event", e)
  }

  private fun handleUserCreated(event: UserCreated) {
    log.debug("Handling user created event: $event")

    commsService.sendTo(
      projectId = event.ownerProject.id,
      userId = event.payload.id,
      templateName = MessageTemplateDefaults.UserCreated.NAME,
      type = commsChannel,
    )
  }

  private fun handleProjectCreated(event: ProjectCreated) {
    log.debug("Handling project created event: $event")

    val owner = creatorService.fetchProjectCreator(event.payload.id)
    commsService.sendTo(
      projectId = event.ownerProject.id,
      userId = owner.id,
      templateName = MessageTemplateDefaults.ProjectCreated.NAME,
      type = commsChannel,
    )
  }

  private fun handleUserAuthResetCompleted(event: UserAuthResetCompleted) {
    log.debug("Handling user auth reset *completed* event: $event")

    commsService.sendTo(
      projectId = event.ownerProject.id,
      userId = event.payload.id,
      overrideUser = event.payload, // to read the raw signature
      templateName = MessageTemplateDefaults.UserAuthResetCompleted.NAME,
      type = commsChannel,
    )
  }

}
