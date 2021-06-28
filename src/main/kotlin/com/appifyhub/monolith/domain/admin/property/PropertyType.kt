package com.appifyhub.monolith.domain.admin.property

enum class PropertyType {

  STRING,
  INTEGER,
  DECIMAL,
  FLAG,

  ;

  companion object {
    fun find(name: String) = values().firstOrNull { it.name == name }
  }

}
