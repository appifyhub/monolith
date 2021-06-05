package com.appifyhub.monolith.domain.admin.property

import com.appifyhub.monolith.domain.admin.property.PropertyTag.GENERIC as TAG_GENERIC
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsNone
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.admin.property.PropertyCategory.GENERIC
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_DECIMAL
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_INTEGER
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_STRING
import com.appifyhub.monolith.domain.admin.property.PropertyTag.COSMETIC
import com.appifyhub.monolith.domain.admin.property.PropertyTag.IMPORTANT
import com.appifyhub.monolith.domain.admin.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyType.STRING
import org.junit.jupiter.api.Test

class PropertyConfigurationTest {

  @Test fun `including generic properties works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true))
      .containsAll(*allGeneric.toTypedArray())
  }

  @Test fun `excluding generic properties works`() {
    assertThat(PropertyConfiguration.findAllWith())
      .containsNone(*allGeneric.toTypedArray())
  }

  private val allGeneric = listOf(GENERIC_STRING, GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG)

  @Test fun `filtering by type works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, type = STRING).strip())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by category works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, category = GENERIC).strip())
      .isEqualTo(allGeneric)
  }

  @Test fun `filtering by name works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, nameContains = "DECIMAL").strip())
      .isEqualTo(listOf(GENERIC_DECIMAL))
  }

  @Test fun `filtering by being mandatory works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, isMandatory = true).strip())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by being secret works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, isSecret = true).strip())
      .isEqualTo(listOf(GENERIC_INTEGER))
  }

  @Test fun `filtering by being read-only works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, isReadOnly = true).strip())
      .isEqualTo(listOf(GENERIC_DECIMAL))
  }

  @Test fun `filtering by being deprecated works`() {
    assertThat(PropertyConfiguration.findAllWith(includeGeneric = true, isDeprecated = true).strip())
      .isEqualTo(listOf(GENERIC_FLAG))
  }

  @Test fun `filtering by having must-have tags works`() {
    assertThat(
      PropertyConfiguration.findAllWith(includeGeneric = true, mustHaveTags = setOf(IMPORTANT, TAG_GENERIC)).strip()
    ).isEqualTo(listOf(GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by having at least one tag works`() {
    assertThat(
      PropertyConfiguration.findAllWith(
        includeGeneric = true,
        hasAtLeastOneOfTags = setOf(IMPORTANT, TAG_GENERIC)
      ).strip()
    ).isEqualTo(listOf(GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by all properties works`() {
    assertThat(
      PropertyConfiguration.findAllWith(
        type = FLAG,
        category = GENERIC,
        nameContains = "FLAG",
        isMandatory = false,
        isSecret = false,
        isReadOnly = false,
        isDeprecated = true,
        mustHaveTags = setOf(IMPORTANT, COSMETIC, TAG_GENERIC),
        hasAtLeastOneOfTags = setOf(TAG_GENERIC),
        includeGeneric = true,
      ).strip()
    ).isEqualTo(listOf(GENERIC_FLAG))
  }

  // Helpers 

  private fun List<PropertyConfiguration>.strip() = filter { it in allGeneric }

}
