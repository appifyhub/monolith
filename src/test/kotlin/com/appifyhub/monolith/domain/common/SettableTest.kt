package com.appifyhub.monolith.domain.common

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import org.junit.jupiter.api.Test

class SettableTest {

  private data class Object(val i: Int)

  @Test fun `applying null settable to object returns the original object`() {
    val obj = Object(0)
    val settable: Settable<Int>? = null

    val result: Object = obj.applySettable(settable) { copy(i = it) }

    assertThat(result).isDataClassEqualTo(obj)
  }

  @Test fun `applying non-null settable to object runs the action`() {
    val obj = Object(0)
    val settable = Settable(1)

    val result: Object = obj.applySettable(settable) { copy(i = it) }

    assertThat(result).isDataClassEqualTo(Object(1))
  }

  @Test fun `map nullable value with null`() {
    val settable = Settable<Int?>(null)

    val result = settable.mapValueNullable { it + 1 }

    assertThat(result).isDataClassEqualTo(settable)
  }

  @Test fun `map nullable value with non-null`() {
    val settable = Settable<Int?>(1)

    val result = settable.mapValueNullable { it + 1 }

    assertThat(result).isDataClassEqualTo(Settable<Int?>(2))
  }

  @Test fun `map non-nullable value`() {
    val settable = Settable(1)

    val result = settable.mapValueNullable { it + 1 }

    assertThat(result).isDataClassEqualTo(Settable(2))
  }

}
