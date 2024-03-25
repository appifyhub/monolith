package com.appifyhub.monolith.features.user.repository.util

import java.util.UUID

object UserIdGenerator {

  val nextId: String
    get() = interceptor() ?: UUID.randomUUID().toString()

  var interceptor: () -> String? = { null }

}
