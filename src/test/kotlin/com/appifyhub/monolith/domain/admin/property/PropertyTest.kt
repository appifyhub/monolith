package com.appifyhub.monolith.domain.admin.property

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import com.appifyhub.monolith.domain.admin.property.Property.DecimalProp
import com.appifyhub.monolith.domain.admin.property.Property.FlagProp
import com.appifyhub.monolith.domain.admin.property.Property.IntegerProp
import com.appifyhub.monolith.domain.admin.property.Property.StringProp
import com.appifyhub.monolith.domain.admin.property.ProjectProperty.GENERIC_DECIMAL
import com.appifyhub.monolith.domain.admin.property.ProjectProperty.GENERIC_FLAG
import com.appifyhub.monolith.domain.admin.property.ProjectProperty.GENERIC_INTEGER
import com.appifyhub.monolith.domain.admin.property.ProjectProperty.GENERIC_STRING
import java.util.Date
import org.junit.jupiter.api.Test

class PropertyTest {

  // region String property

  @Test fun `string property has value if raw is valid`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = "valid", Date())

    assertThat { prop.typed() }
      .isSuccess()
  }

  @Test fun `string property required value succeeds if raw is valid`() {
    val prop: Property<String> = StringProp(GENERIC_STRING, 0, rawValue = "valid", Date())

    assertThat(prop.typed())
      .isEqualTo("valid")
  }

  // endregion

  // region Integer property

  @Test fun `integer property has no value if malformed`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
  }

  @Test fun `integer property has value if raw is valid`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "1", Date())

    assertThat { prop.typed() }
      .isSuccess()
  }

  @Test fun `integer property required value throws if malformed`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `integer property required value succeeds if raw is valid`() {
    val prop: Property<Int> = IntegerProp(GENERIC_INTEGER, 0, rawValue = "1", Date())

    assertThat(prop.typed())
      .isEqualTo(1)
  }

  // endregion

  // region Decimal property

  @Test fun `decimal property has no value if malformed`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
  }

  @Test fun `decimal property has value if raw is valid`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "1.30", Date())

    assertThat { prop.typed() }
      .isSuccess()
  }

  @Test fun `decimal property required value throws if malformed`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `decimal property required value succeeds if raw is valid`() {
    val prop: Property<Double> = DecimalProp(GENERIC_DECIMAL, 0, rawValue = "1.30", Date())

    assertThat(prop.typed())
      .isEqualTo(1.30)
  }

  // endregion

  // region Flag property

  @Test fun `flag property has no value if malformed`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
  }

  @Test fun `flag property has value if raw is valid - true`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "true", Date())

    assertThat { prop.typed() }
      .isSuccess()
  }

  @Test fun `flag property has value if raw is valid - false`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "false", Date())

    assertThat { prop.typed() }
      .isSuccess()
  }

  @Test fun `flag property required value throws if malformed`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "malformed", Date())

    assertThat { prop.typed() }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
      }
  }

  @Test fun `flag property required value succeeds if raw is valid - true`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "true", Date())

    assertThat(prop.typed())
      .isEqualTo(true)
  }

  @Test fun `flag property required value succeeds if raw is valid - false`() {
    val prop: Property<Boolean> = FlagProp(GENERIC_FLAG, 0, rawValue = "false", Date())

    assertThat(prop.typed())
      .isEqualTo(false)
  }

  // endregion

}
