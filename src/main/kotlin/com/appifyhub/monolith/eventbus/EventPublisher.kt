package com.appifyhub.monolith.eventbus

import org.springframework.scheduling.annotation.Async

interface EventPublisher {

  @Async fun publish(event: ApplicationEvent<*>)

}
