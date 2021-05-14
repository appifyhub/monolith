package com.appifyhub.monolith.repository.admin

import assertk.all
import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.length
import org.junit.jupiter.api.Test

class SignatureGeneratorTest {

  @Test fun `signatures are UUIDs without dashes`() {
    val signature = SignatureGenerator.nextSignature
    assertThat(signature).all {
      length().isEqualTo(32)
      doesNotContain("-")
    }
  }

  @Test fun `signature interceptor replaces random signatures`() {
    SignatureGenerator.interceptor = { "signature" }
    val signature = SignatureGenerator.nextSignature
    assertThat(signature).isEqualTo("signature")
    SignatureGenerator.interceptor = { null } // for other tests
  }

}
