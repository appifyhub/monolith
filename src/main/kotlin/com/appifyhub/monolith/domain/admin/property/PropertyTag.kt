package com.appifyhub.monolith.domain.admin.property

enum class PropertyTag {

  IMPORTANT,
  COSMETIC,
  CONSTRAINTS,
  GENERIC,

  ;

  companion object {
    fun find(name: String) = values().firstOrNull { it.name == name }
  }

}
