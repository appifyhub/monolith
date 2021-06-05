package com.appifyhub.monolith.domain.admin.property

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.admin.property.Property.DecimalProp
import com.appifyhub.monolith.domain.admin.property.Property.FlagProp
import com.appifyhub.monolith.domain.admin.property.Property.IntegerProp
import com.appifyhub.monolith.domain.admin.property.Property.StringProp
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_DECIMAL
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_INTEGER
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_STRING
import java.util.Date
import org.junit.jupiter.api.Test

class PropertyTest {

  // region String property

  @Test fun `string property has no value if null`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = null, Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `string property has value if raw is valid`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = "valid", Date())

    assertThat(prop.hasValue())
      .isTrue()
  }

  @Test fun `string property required value throws if null`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = null, Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(IllegalStateException::class)
        messageContains("No value found")
      }
  }

  @Test fun `string property required value succeeds if raw is valid`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = "valid", Date())

    assertThat(prop.requireValue())
      .isEqualTo("valid")
  }

  // endregion

  // region Integer property

  @Test fun `integer property has no value if null`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = null, Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `integer property has no value if malformed`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "malformed", Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `integer property has value if raw is valid`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "1", Date())

    assertThat(prop.hasValue())
      .isTrue()
  }

  @Test fun `integer property required value throws if null`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = null, Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(IllegalStateException::class)
        messageContains("No value found")
      }
  }

  @Test fun `integer property required value throws if malformed`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "malformed", Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `integer property required value succeeds if raw is valid`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "1", Date())

    assertThat(prop.requireValue())
      .isEqualTo(1)
  }

  // endregion

  // region Decimal property

  @Test fun `decimal property has no value if null`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = null, Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `decimal property has no value if malformed`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "malformed", Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `decimal property has value if raw is valid`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "1.30", Date())

    assertThat(prop.hasValue())
      .isTrue()
  }

  @Test fun `decimal property required value throws if null`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = null, Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(IllegalStateException::class)
        messageContains("No value found")
      }
  }

  @Test fun `decimal property required value throws if malformed`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "malformed", Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `decimal property required value succeeds if raw is valid`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "1.30", Date())

    assertThat(prop.requireValue())
      .isEqualTo(1.30)
  }

  // endregion

  // region Flag property

  @Test fun `flag property has no value if null`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = null, Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `flag property has no value if malformed`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "malformed", Date())

    assertThat(prop.hasValue())
      .isFalse()
  }

  @Test fun `flag property has value if raw is valid - true`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "true", Date())

    assertThat(prop.hasValue())
      .isTrue()
  }

  @Test fun `flag property has value if raw is valid - false`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "false", Date())

    assertThat(prop.hasValue())
      .isTrue()
  }

  @Test fun `flag property required value throws if null`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = null, Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(IllegalStateException::class)
        messageContains("No value found")
      }
  }

  @Test fun `flag property required value throws if malformed`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "malformed", Date())

    assertThat { prop.requireValue() }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
      }
  }

  @Test fun `flag property required value succeeds if raw is valid - true`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "true", Date())

    assertThat(prop.requireValue())
      .isEqualTo(true)
  }

  @Test fun `flag property required value succeeds if raw is valid - false`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "false", Date())

    assertThat(prop.requireValue())
      .isEqualTo(false)
  }

  // endregion

}
