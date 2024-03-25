package com.appifyhub.monolith.features.user.api

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class SignupCodeMapperTest {

  @Test fun `signup code domain to network`() {
    assertThat(Stubs.signupCode.toNetwork())
      .isDataClassEqualTo(Stubs.signupCodeResponse)
  }

  @Test fun `signup code collection domain to network`() {
    assertThat(listOf(Stubs.signupCode).toNetwork(Stubs.project.maxSignupCodesPerUser))
      .isDataClassEqualTo(Stubs.signupCodesResponse)
  }

}
