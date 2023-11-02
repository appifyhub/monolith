package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.eventbus.ApplicationEvent
import com.appifyhub.monolith.eventbus.EventPublisher
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Primary
@Component
@Profile(TestAppifyHubApplication.PROFILE)
@Suppress("unused")
class EventBusFake : EventPublisher {

  private val publishList = mutableListOf<ApplicationEvent<*>>()

  val firstPublished: ApplicationEvent<*>?
    get() = publishList.firstOrNull()

  val lastPublished: ApplicationEvent<*>?
    get() = publishList.lastOrNull()

  val nextPublished: ApplicationEvent<*>?
    get() = publishList.removeFirstOrNull()

  override fun publish(event: ApplicationEvent<*>) {
    publishList += event
  }

}
