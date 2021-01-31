package com.appifyhub.monolith.util.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class StringExtensionsTest {

  @Test fun `takeIfNotBlank with empty string gives null`() {
    assertThat("".takeIfNotBlank()).isEqualTo(null)
  }

  @Test fun `takeIfNotBlank with space gives null`() {
    assertThat(" ".takeIfNotBlank()).isEqualTo(null)
  }

  @Test fun `takeIfNotBlank with content gives content`() {
    assertThat("content".takeIfNotBlank()).isEqualTo("content")
  }

  @Test fun `isNullOrNotBlank with empty string gives false`() {
    assertThat("".isNullOrNotBlank()).isEqualTo(false) // it's blank
  }

  @Test fun `isNullOrNotBlank with space gives false`() {
    assertThat("".isNullOrNotBlank()).isEqualTo(false) // it's blank
  }

  @Test fun `isNullOrNotBlank with null gives true`() {
    assertThat(null.isNullOrNotBlank()).isEqualTo(true) // it's null
  }

  @Test fun `isNullOrNotBlank with content gives true`() {
    assertThat("content".isNullOrNotBlank()).isEqualTo(true) // has content
  }

  @Test fun `hasSpaces with spaces gives true`() {
    assertThat("with spaces".hasSpaces()).isEqualTo(true)
  }

  @Test fun `hasSpaces without spaces gives false`() {
    assertThat("nothing".hasSpaces()).isEqualTo(false)
  }

  @Test fun `hasNoSpaces with spaces gives false`() {
    assertThat("with spaces".hasNoSpaces()).isEqualTo(false)
  }

  @Test fun `hasNoSpaces without spaces gives true`() {
    assertThat("nothing".hasNoSpaces()).isEqualTo(true)
  }

}