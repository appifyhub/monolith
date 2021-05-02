package com.appifyhub.monolith.storage.model.user

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class UserIdDbm(

  @Column(nullable = false, updatable = false, length = 128)
  var userId: String,

  @Column(nullable = false, updatable = false)
  var projectId: Long,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UserIdDbm) return false

    if (userId != other.userId) return false
    if (projectId != other.projectId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + projectId.hashCode()
    return result
  }

  override fun toString(): String {
    return "UserIdDbm(" +
      "userId='$userId', " +
      "projectId=$projectId" +
      ")"
  }

}
