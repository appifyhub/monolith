package com.appifyhub.monolith.storage.model.creator

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class PropertyIdDbm(

  @Column(nullable = false, updatable = false, length = 128)
  var name: String,

  @Column(nullable = false, updatable = false)
  var projectId: Long,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PropertyIdDbm) return false

    if (name != other.name) return false
    if (projectId != other.projectId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + projectId.hashCode()
    return result
  }

  override fun toString(): String {
    return "PropertyIdDbm(" +
      "name='$name', " +
      "projectId=$projectId" +
      ")"
  }

}
