package com.appifyhub.monolith.features.user.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class SignupCodeMapperTest {

  @Test fun `signup code data to domain`() {
    assertThat(Stubs.signupCodeDbm.toDomain()).isDataClassEqualTo(Stubs.signupCode)
  }

  @Test fun `signup code domain to data`() {
    assertThat(Stubs.signupCode.toData(Stubs.project)).isEqualTo(Stubs.signupCodeDbm)
  }

}
