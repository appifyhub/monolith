package com.appifyhub.monolith.eventbus

import org.springframework.context.event.EventListener

interface EventSubscriber {

  @EventListener fun onEvent(event: ApplicationEvent<*>)

}
