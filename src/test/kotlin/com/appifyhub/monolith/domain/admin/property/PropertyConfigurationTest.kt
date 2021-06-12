package com.appifyhub.monolith.domain.admin.property

import com.appifyhub.monolith.domain.admin.property.PropertyTag.GENERIC as TAG_GENERIC
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsNone
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.admin.ops.PropertyFilter
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
    assertThat(PropertyConfiguration.filter(includeGeneric = true))
      .containsAll(*allGeneric.toTypedArray())
  }

  @Test fun `excluding generic properties works`() {
    assertThat(PropertyConfiguration.filter())
      .containsNone(*allGeneric.toTypedArray())
  }

  private val allGeneric = listOf(GENERIC_STRING, GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG)

  @Test fun `filtering by type works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(type = STRING), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by category works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(category = GENERIC), includeGeneric = true).forTest())
      .isEqualTo(allGeneric)
  }

  @Test fun `filtering by name works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(nameContains = "DECIMAL"), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_DECIMAL))
  }

  @Test fun `filtering by being mandatory works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(isMandatory = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by being secret works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(isSecret = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_INTEGER))
  }

  @Test fun `filtering by being deprecated works`() {
    assertThat(PropertyConfiguration.filter(PropertyFilter(isDeprecated = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_FLAG))
  }

  @Test fun `filtering by having must-have tags works`() {
    assertThat(
      PropertyConfiguration.filter(
        PropertyFilter(mustHaveTags = setOf(IMPORTANT, TAG_GENERIC)),
        includeGeneric = true
      ).forTest()
    ).isEqualTo(listOf(GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by having at least one tag works`() {
    assertThat(
      PropertyConfiguration.filter(
        PropertyFilter(hasAtLeastOneOfTags = setOf(IMPORTANT, TAG_GENERIC)),
        includeGeneric = true
      ).forTest()
    ).isEqualTo(listOf(GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by all properties works`() {
    assertThat(
      PropertyConfiguration.filter(
        PropertyFilter(
          type = FLAG,
          category = GENERIC,
          nameContains = "FLAG",
          isMandatory = false,
          isSecret = false,
          isDeprecated = true,
          mustHaveTags = setOf(IMPORTANT, COSMETIC, TAG_GENERIC),
          hasAtLeastOneOfTags = setOf(TAG_GENERIC),
        ),
        includeGeneric = true,
      ).forTest()
    ).isEqualTo(listOf(GENERIC_FLAG))
  }

  // Helpers 

  private fun List<PropertyConfiguration>.forTest() = filter { it in allGeneric }

}
