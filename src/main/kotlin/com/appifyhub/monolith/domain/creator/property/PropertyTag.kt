package com.appifyhub.monolith.domain.creator.property

enum class PropertyTag {

  IMPORTANT,
  COSMETIC,
  CONSTRAINTS,
  CONTROL,
  GENERIC,

  ;

  companion object {
    fun findOrNull(name: String) = values().firstOrNull { it.name == name }
  }

}
