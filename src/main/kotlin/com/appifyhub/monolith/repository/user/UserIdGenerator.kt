package com.appifyhub.monolith.repository.user

import java.util.UUID

object UserIdGenerator {

  val nextId: String
    get() = interceptor() ?: UUID.randomUUID().toString()

  var interceptor: () -> String? = { null }

}