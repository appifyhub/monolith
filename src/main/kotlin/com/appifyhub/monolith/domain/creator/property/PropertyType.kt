package com.appifyhub.monolith.domain.creator.property

enum class PropertyType {

  STRING,
  INTEGER,
  DECIMAL,
  FLAG,

  ;

  companion object {
    fun findOrNull(name: String) = values().firstOrNull { it.name == name }
  }

}
