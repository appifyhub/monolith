package com.appifyhub.monolith.domain.creator.property

import com.appifyhub.monolith.domain.creator.property.PropertyTag.GENERIC as TAG_GENERIC
import com.appifyhub.monolith.domain.creator.property.PropertyCategory.GENERIC
import com.appifyhub.monolith.domain.creator.property.PropertyCategory.IDENTITY
import com.appifyhub.monolith.domain.creator.property.PropertyCategory.OPERATIONAL
import com.appifyhub.monolith.domain.creator.property.PropertyTag.CONSTRAINTS
import com.appifyhub.monolith.domain.creator.property.PropertyTag.CONTROL
import com.appifyhub.monolith.domain.creator.property.PropertyTag.COSMETIC
import com.appifyhub.monolith.domain.creator.property.PropertyTag.IMPORTANT
import com.appifyhub.monolith.domain.creator.property.PropertyType.DECIMAL
import com.appifyhub.monolith.domain.creator.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.creator.property.PropertyType.INTEGER
import com.appifyhub.monolith.domain.creator.property.PropertyType.STRING
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.validation.Normalizer
import com.appifyhub.monolith.validation.impl.Normalizers

@Suppress("unused") // most of these won't be used directly but located by name
enum class ProjectProperty(
  val type: PropertyType,
  val category: PropertyCategory,
  val normalizer: Normalizer<String>,
  val tags: Set<PropertyTag>,
  val defaultValue: String,
  val isMandatory: Boolean,
  val isSecret: Boolean,
  val isDeprecated: Boolean,
) {

  // region Identity

  NAME(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectName,
    tags = setOf(IMPORTANT),
    defaultValue = "The Best Project Ever",
    isMandatory = true,
    isSecret = false,
    isDeprecated = false,
  ),

  DESCRIPTION(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectDescription,
    tags = setOf(COSMETIC),
    defaultValue = "This project is about doing the best work I can do.",
    isMandatory = false,
    isSecret = false,
    isDeprecated = false,
  ),

  LOGO_URL(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectLogoUrl,
    tags = setOf(COSMETIC),
    defaultValue = "https://i.imgur.com/4mcQDdB.png",
    isMandatory = false,
    isSecret = false,
    isDeprecated = false,
  ),

  WEBSITE_URL(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectWebsite,
    tags = setOf(COSMETIC),
    defaultValue = "https://www.appifyhub.com",
    isMandatory = false,
    isSecret = false,
    isDeprecated = false,
  ),

  // endregion

  // region Operational

  MAX_USERS(
    type = INTEGER,
    category = OPERATIONAL,
    normalizer = Normalizers.PropMaxUsers,
    tags = setOf(CONSTRAINTS, IMPORTANT),
    defaultValue = "100 000",
    isMandatory = false,
    isSecret = false,
    isDeprecated = false,
  ),

  ANYONE_CAN_SEARCH(
    type = FLAG,
    category = OPERATIONAL,
    normalizer = Normalizers.PropOnHold,
    tags = setOf(CONTROL, IMPORTANT),
    defaultValue = "true",
    isMandatory = true,
    isSecret = false,
    isDeprecated = false,
  ),

  ON_HOLD(
    type = FLAG,
    category = OPERATIONAL,
    normalizer = Normalizers.PropOnHold,
    tags = setOf(CONTROL, IMPORTANT),
    defaultValue = "true",
    isMandatory = true,
    isSecret = false,
    isDeprecated = false,
  ),

  // endregion

  // region Generic

  GENERIC_STRING(
    type = STRING,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = emptySet(),
    defaultValue = "A string",
    isMandatory = true,
    isSecret = false,
    isDeprecated = false,
  ),

  GENERIC_INTEGER(
    type = INTEGER,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(TAG_GENERIC),
    defaultValue = "An integer",
    isMandatory = false,
    isSecret = true,
    isDeprecated = false,
  ),

  GENERIC_DECIMAL(
    type = DECIMAL,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(IMPORTANT, TAG_GENERIC),
    defaultValue = "A decimal",
    isMandatory = false,
    isSecret = false,
    isDeprecated = false,
  ),

  GENERIC_FLAG(
    type = FLAG,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(IMPORTANT, COSMETIC, TAG_GENERIC),
    defaultValue = "A flag",
    isMandatory = false,
    isSecret = false,
    isDeprecated = true,
  ),

  // endregion

  ;

  companion object {

    fun names() = values().map(ProjectProperty::name)

    fun findOrNull(name: String): ProjectProperty? = values().firstOrNull { it.name == name }

    fun filter(
      filter: PropertyFilter? = null,
      includeGeneric: Boolean = false,
    ): List<ProjectProperty> =
      values()
        .asSequence()
        .filter { includeGeneric || it.category != GENERIC }
        .filterWith(filter?.type) { type == it }
        .filterWith(filter?.category) { category == it }
        .filterWith(filter?.nameContains) { name.contains(it, ignoreCase = true) }
        .filterWith(filter?.isMandatory) { isMandatory == it }
        .filterWith(filter?.isSecret) { isSecret == it }
        .filterWith(filter?.isDeprecated) { isDeprecated == it }
        .filterWith(filter?.mustHaveTags) { tags.containsAll(it) }
        .filterWith(filter?.hasAtLeastOneOfTags) { tags.any(it::contains) }
        .toList()

    private inline fun <T : Any> Sequence<ProjectProperty>.filterWith(
      filterValue: T?,
      crossinline predicate: ProjectProperty.(T) -> Boolean,
    ): Sequence<ProjectProperty> = filter { config ->
      filterValue?.let { config.predicate(it) } ?: true
    }

  }

}
