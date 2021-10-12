package com.appifyhub.monolith.domain.creator.property

enum class PropertyCategory {

  IDENTITY,
  OPERATIONAL,
  GENERIC,

  ;

  companion object {
    fun find(name: String) = values().firstOrNull { it.name == name }
  }

}
