package com.appifyhub.monolith.domain.creator.property

import com.appifyhub.monolith.domain.creator.property.PropertyTag.GENERIC as TAG_GENERIC
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsNone
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.domain.creator.property.PropertyCategory.GENERIC
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_DECIMAL
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_FLAG
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_INTEGER
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_STRING
import com.appifyhub.monolith.domain.creator.property.PropertyTag.COSMETIC
import com.appifyhub.monolith.domain.creator.property.PropertyTag.IMPORTANT
import com.appifyhub.monolith.domain.creator.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.creator.property.PropertyType.STRING
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import org.junit.jupiter.api.Test

class ProjectPropertyTest {

  @Test fun `including generic properties works`() {
    assertThat(ProjectProperty.filter(includeGeneric = true))
      .containsAll(*allGeneric.toTypedArray())
  }

  @Test fun `excluding generic properties works`() {
    assertThat(ProjectProperty.filter())
      .containsNone(*allGeneric.toTypedArray())
  }

  private val allGeneric = listOf(GENERIC_STRING, GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG)

  @Test fun `filtering by type works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(type = STRING), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by category works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(category = GENERIC), includeGeneric = true).forTest())
      .isEqualTo(allGeneric)
  }

  @Test fun `filtering by name works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(nameContains = "DECIMAL"), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_DECIMAL))
  }

  @Test fun `filtering by being mandatory works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(isMandatory = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_STRING))
  }

  @Test fun `filtering by being secret works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(isSecret = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_INTEGER))
  }

  @Test fun `filtering by being deprecated works`() {
    assertThat(ProjectProperty.filter(PropertyFilter(isDeprecated = true), includeGeneric = true).forTest())
      .isEqualTo(listOf(GENERIC_FLAG))
  }

  @Test fun `filtering by having must-have tags works`() {
    assertThat(
      ProjectProperty.filter(
        PropertyFilter(mustHaveTags = setOf(IMPORTANT, TAG_GENERIC)),
        includeGeneric = true,
      ).forTest()
    ).isEqualTo(listOf(GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by having at least one tag works`() {
    assertThat(
      ProjectProperty.filter(
        PropertyFilter(hasAtLeastOneOfTags = setOf(IMPORTANT, TAG_GENERIC)),
        includeGeneric = true,
      ).forTest()
    ).isEqualTo(listOf(GENERIC_INTEGER, GENERIC_DECIMAL, GENERIC_FLAG))
  }

  @Test fun `filtering by all properties works`() {
    assertThat(
      ProjectProperty.filter(
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

  private fun List<ProjectProperty>.forTest() = filter { it in allGeneric }

}
