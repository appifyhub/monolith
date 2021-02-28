package com.appifyhub.monolith.repository.auth.locator

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import com.appifyhub.monolith.domain.user.UserId
import org.junit.jupiter.api.Test

class TokenLocatorCoderImplTest {

  private val encoder: TokenLocatorEncoder = TokenLocatorCoderImpl()
  private val decoder: TokenLocatorDecoder = encoder as TokenLocatorCoderImpl

  @Test fun `encoding with empty ID works`() {
    val locator = TokenLocator(
      userId = UserId(
        id = "",
        projectId = 2,
      ),
      origin = "origin",
      timestamp = 10,
    )

    assertThat { encoder.encode(locator) }
      .isSuccess()
  }

  @Test fun `coding with null origin works`() {
    val locator = TokenLocator(
      userId = UserId(
        id = "username",
        projectId = 2,
      ),
      origin = null,
      timestamp = 10,
    )

    val encoded = encoder.encode(locator)
    assertThat(decoder.decode(encoded))
      .isDataClassEqualTo(locator)
  }

  @Test fun `coding with empty origin works`() {
    val locator = TokenLocator(
      userId = UserId(
        id = "username",
        projectId = 2,
      ),
      origin = "",
      timestamp = 10,
    )

    val encoded = encoder.encode(locator)
    assertThat(decoder.decode(encoded))
      .isDataClassEqualTo(locator.copy(origin = null))
  }

  @Test fun `coding with all properties works`() {
    val locator = TokenLocator(
      userId = UserId(
        id = "username",
        projectId = 2,
      ),
      origin = "origin",
      timestamp = 10,
    )

    val encoded = encoder.encode(locator)
    assertThat(decoder.decode(encoded))
      .isDataClassEqualTo(locator)
  }

  @Test fun `decoding with empty user ID throws`() {
    val locator = TokenLocator(
      userId = UserId(
        id = "",
        projectId = 2,
      ),
      origin = "origin",
      timestamp = 10,
    )
    val encoded = encoder.encode(locator)

    assertThat { decoder.decode(encoded) }
      .isFailure()
      .hasClass(IllegalArgumentException::class)
  }

  @Test fun `decoding malformed token locator throws`() {
    assertThat { decoder.decode("malformed") }
      .isFailure()
      .hasClass(IllegalArgumentException::class)
  }

}