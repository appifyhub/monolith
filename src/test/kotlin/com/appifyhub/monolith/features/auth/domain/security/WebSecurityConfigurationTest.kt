package com.appifyhub.monolith.features.auth.domain.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.errors.GlobalExceptionHandler
import com.appifyhub.monolith.features.user.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class WebSecurityConfigurationTest {

  private val userRepository = mock<UserRepository>()
  private val exceptionHandler = mock<GlobalExceptionHandler>()

  private val configuration = WebSecurityConfiguration(
    userRepository = userRepository,
    exceptionHandler = exceptionHandler,
  )

  @Test fun `secure user details service is user repo`() {
    assertThat(configuration.userDetailsService()).isEqualTo(userRepository)
  }

}
