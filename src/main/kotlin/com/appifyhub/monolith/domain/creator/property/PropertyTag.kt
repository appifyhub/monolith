package com.appifyhub.monolith.domain.creator.property

enum class PropertyTag {

  IMPORTANT,
  COSMETIC,
  CONSTRAINTS,
  CONTROL,
  GENERIC,

  ;

  companion object {
    fun find(name: String) = values().firstOrNull { it.name == name }
  }

}
