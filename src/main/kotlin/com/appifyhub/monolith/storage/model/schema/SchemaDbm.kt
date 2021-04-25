package com.appifyhub.monolith.storage.model.schema

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "schema")
class SchemaDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false)
  var version: Long,

  @Column(nullable = false)
  var isInitialized: Boolean,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SchemaDbm) return false

    if (version != other.version) return false
    if (isInitialized != other.isInitialized) return false

    return true
  }

  override fun hashCode(): Int {
    var result = version.hashCode()
    result = 31 * result + isInitialized.hashCode()
    return result
  }

  override fun toString(): String {
    return "SchemaDbm(" +
      "version=$version, " +
      "isInitialized=$isInitialized" +
      ")"
  }

}
