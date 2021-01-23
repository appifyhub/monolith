package com.appifyhub.monolith.storage.model.user

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class UserIdDbm(

  @Column(nullable = false, updatable = false, length = 128)
  var identifier: String,

  @Column(nullable = false, updatable = false)
  var projectId: Long,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UserIdDbm) return false

    if (identifier != other.identifier) return false
    if (projectId != other.projectId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + projectId.hashCode()
    return result
  }

}
