package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.util.Stubs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthMapperTest {

  private val jwtHelper = mock<JwtHelper>()

  @BeforeEach fun setup() {
    jwtHelper.stub {
      onGeneric { createJwtForClaims(any(), any(), any(), any()) } doReturn Stubs.tokenValue
      onGeneric { extractPropertiesFromJwt(any()) } doReturn Stubs.jwtClaims
    }
  }

  @Test fun `token details data to domain`() {
    assertThat(Stubs.tokenDetailsDbm.toDomain(jwtHelper))
      .isDataClassEqualTo(Stubs.tokenDetails)
  }

  @Test fun `token details domain to data`() {
    assertThat(Stubs.tokenDetails.toData(Stubs.user, Stubs.project))
      .isEqualTo(Stubs.tokenDetailsDbm)
  }

  @Test fun `jwt claims to token details`() {
    assertThat(Stubs.jwtClaims.toTokenDetails(isBlocked = true))
      .isDataClassEqualTo(Stubs.tokenDetails)
  }

}
