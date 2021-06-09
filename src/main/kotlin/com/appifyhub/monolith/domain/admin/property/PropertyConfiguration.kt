package com.appifyhub.monolith.domain.admin.property

import com.appifyhub.monolith.domain.admin.property.PropertyTag.GENERIC as TAG_GENERIC
import com.appifyhub.monolith.domain.admin.property.PropertyCategory.GENERIC
import com.appifyhub.monolith.domain.admin.property.PropertyCategory.IDENTITY
import com.appifyhub.monolith.domain.admin.property.PropertyTag.COSMETIC
import com.appifyhub.monolith.domain.admin.property.PropertyTag.IMPORTANT
import com.appifyhub.monolith.domain.admin.property.PropertyType.DECIMAL
import com.appifyhub.monolith.domain.admin.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyType.INTEGER
import com.appifyhub.monolith.domain.admin.property.PropertyType.STRING
import com.appifyhub.monolith.validation.Normalizer
import com.appifyhub.monolith.validation.impl.Normalizers

enum class PropertyConfiguration(
  val type: PropertyType,
  val category: PropertyCategory,
  val normalizer: Normalizer<*>,
  val tags: Set<PropertyTag>,
  val isMandatory: Boolean,
  val isSecret: Boolean,
  val isReadOnly: Boolean,
  val isDeprecated: Boolean,
) {

  // region Identity

  PROJECT_NAME(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectName,
    tags = setOf(IMPORTANT),
    isMandatory = true,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = false,
  ),

  PROJECT_DESCRIPTION(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectDescription,
    tags = setOf(COSMETIC),
    isMandatory = false,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = false,
  ),

  PROJECT_LOGO_URL(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectLogoUrl,
    tags = setOf(COSMETIC),
    isMandatory = false,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = false,
  ),

  PROJECT_WEBSITE(
    type = STRING,
    category = IDENTITY,
    normalizer = Normalizers.PropProjectWebsite,
    tags = setOf(COSMETIC),
    isMandatory = false,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = false,
  ),

  // endregion

  // region Generic 

  GENERIC_STRING(
    type = STRING,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = emptySet(),
    isMandatory = true,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = false,
  ),

  GENERIC_INTEGER(
    type = INTEGER,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(TAG_GENERIC),
    isMandatory = false,
    isSecret = true,
    isReadOnly = false,
    isDeprecated = false,
  ),

  GENERIC_DECIMAL(
    type = DECIMAL,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(IMPORTANT, TAG_GENERIC),
    isMandatory = false,
    isSecret = false,
    isReadOnly = true,
    isDeprecated = false,
  ),

  GENERIC_FLAG(
    type = FLAG,
    category = GENERIC,
    normalizer = Normalizers.NotBlank,
    tags = setOf(IMPORTANT, COSMETIC, TAG_GENERIC),
    isMandatory = false,
    isSecret = false,
    isReadOnly = false,
    isDeprecated = true,
  ),

  // endregion

  ;

  companion object {

    fun find(name: String) = values().firstOrNull { it.name == name }

    fun findAllWith(
      type: PropertyType? = null,
      category: PropertyCategory? = null,
      nameContains: String? = null,
      isMandatory: Boolean? = null,
      isSecret: Boolean? = null,
      isReadOnly: Boolean? = null,
      isDeprecated: Boolean? = null,
      mustHaveTags: Set<PropertyTag>? = null,
      hasAtLeastOneOfTags: Set<PropertyTag>? = null,
      includeGeneric: Boolean = false,
    ): List<PropertyConfiguration> =
      values()
        .asSequence()
        .filter { includeGeneric || it.category != GENERIC }
        .filterWith(type) { this.type == it }
        .filterWith(category) { this.category == it }
        .filterWith(nameContains) { this.name.contains(it) }
        .filterWith(isMandatory) { this.isMandatory == it }
        .filterWith(isSecret) { this.isSecret == it }
        .filterWith(isReadOnly) { this.isReadOnly == it }
        .filterWith(isDeprecated) { this.isDeprecated == it }
        .filterWith(mustHaveTags) { this.tags.containsAll(it) }
        .filterWith(hasAtLeastOneOfTags) { this.tags.any(it::contains) }
        .toList()

    private inline fun <T : Any> Sequence<PropertyConfiguration>.filterWith(
      filterValue: T?,
      crossinline predicate: PropertyConfiguration.(T) -> Boolean,
    ): Sequence<PropertyConfiguration> = filter { config ->
      filterValue?.let { config.predicate(it) } ?: true
    }

  }

}
