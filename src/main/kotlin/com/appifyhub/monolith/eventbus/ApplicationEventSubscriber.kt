package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.integrations.CommunicationsService
import com.appifyhub.monolith.service.messaging.MessageTemplateService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventSubscriber(
  private val commsService: CommunicationsService,
  private val creatorService: CreatorService,
) : EventSubscriber {

  private val log = LoggerFactory.getLogger(this::class.java)

  @EventListener
  override fun onEvent(event: ApplicationEvent<*>) = try {
    when (event) {
      is ProjectCreated -> handleProjectCreated(event)
    }
  } catch (e: Exception) {
    log.warn("Failed to handle event: $event", e)
  }

  private fun handleProjectCreated(event: ProjectCreated) {
    log.debug("Handling project created event: $event")

    val owner = creatorService.fetchProjectCreator(event.payload.id)
    commsService.sendTo(
      projectId = event.ownerProject.id,
      userId = owner.id,
      templateName = MessageTemplateService.NAME_PROJECT_CREATED,
      type = CommunicationsService.Type.EMAIL, // opinionated choice
    )
  }

}
