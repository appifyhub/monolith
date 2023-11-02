package com.appifyhub.monolith.eventbus

import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EventPublisherImpl(
  private val systemPublisher: ApplicationEventPublisher,
) : EventPublisher {

  @Async
  override fun publish(event: ApplicationEvent<*>) = systemPublisher.publishEvent(event)

}
