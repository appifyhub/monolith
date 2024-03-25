package com.appifyhub.monolith.features.user.repository.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.size
import org.junit.jupiter.api.Test
import java.util.UUID

class UserIdGeneratorTest {

  @Test fun `user ID does not repeat`() {
    val ids = mutableSetOf<String>()
    for (i in 1..1000) {
      ids += UserIdGenerator.nextId
    }
    assertThat(ids).size().isEqualTo(1000)
  }

  @Test fun `user ID is a UUID`() {
    val id = UserIdGenerator.nextId
    val uuid = UUID.fromString(id)
    assertThat(uuid).isNotNull()
  }

  @Test fun `user ID interceptor replaces random IDs`() {
    UserIdGenerator.interceptor = { "id" }
    val id = UserIdGenerator.nextId
    assertThat(id).isEqualTo("id")
    UserIdGenerator.interceptor = { null } // for other tests
  }

}
