package com.appifyhub.monolith.features.user.storage.model

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

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UserIdDbm) return false

    if (userId != other.userId) return false
    if (projectId != other.projectId) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + projectId.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "UserIdDbm(" +
      "userId='$userId', " +
      "projectId=$projectId" +
      ")"
  }

}
