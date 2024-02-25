package com.appifyhub.monolith.repository.user

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isEqualTo
import assertk.assertions.length
import org.junit.jupiter.api.Test

class SignupCodeGeneratorTest {

  @Test fun `signup codes are 14-UUIDs`() {
    val signupCode = SignupCodeGenerator.nextCode
    assertThat(signupCode).all {
      length().isEqualTo(14)
      transform { it.split("-") }.each {
        it.length().isEqualTo(4)
      }
    }
  }

  @Test fun `interceptor replaces random signup codes`() {
    SignupCodeGenerator.interceptor = { "code" }
    val signupCode = SignupCodeGenerator.nextCode
    assertThat(signupCode).isEqualTo("code")
    SignupCodeGenerator.interceptor = { null } // for other tests
  }

}
