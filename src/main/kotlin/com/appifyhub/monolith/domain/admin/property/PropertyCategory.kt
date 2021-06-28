package com.appifyhub.monolith.domain.admin.property

enum class PropertyCategory {

  IDENTITY,
  OPERATIONAL,
  GENERIC,

  ;

  companion object {
    fun find(name: String) = values().firstOrNull { it.name == name }
  }

}
