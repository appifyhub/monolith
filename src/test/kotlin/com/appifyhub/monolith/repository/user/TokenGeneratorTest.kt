package com.appifyhub.monolith.repository.user

import assertk.all
import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.length
import org.junit.jupiter.api.Test

class TokenGeneratorTest {

  @Test fun `email tokens are 16-UUIDs`() {
    val token = TokenGenerator.nextEmailToken
    assertThat(token).all {
      length().isEqualTo(16)
      doesNotContain("-")
    }
  }

  @Test fun `phone tokens are 6 digits`() {
    val token = TokenGenerator.nextPhoneToken
    assertThat(token).all {
      length().isEqualTo(6)
      transform { it.toInt() }.all {
        isLessThan(1000000)
        isGreaterThanOrEqualTo(0)
      }
    }
  }

  @Test fun `email interceptor replaces random tokens`() {
    TokenGenerator.emailInterceptor = { "token" }
    val token = TokenGenerator.nextEmailToken
    assertThat(token).isEqualTo("token")
    TokenGenerator.emailInterceptor = { null } // for other tests
  }

  @Test fun `phone interceptor replaces random tokens`() {
    TokenGenerator.phoneInterceptor = { "token" }
    val token = TokenGenerator.nextPhoneToken
    assertThat(token).isEqualTo("token")
    TokenGenerator.phoneInterceptor = { null } // for other tests
  }

}