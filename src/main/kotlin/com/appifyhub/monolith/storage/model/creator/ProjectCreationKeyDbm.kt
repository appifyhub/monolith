package com.appifyhub.monolith.storage.model.creator

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class ProjectCreationKeyDbm(

  // coming from user

  @Column(name = "creator_user_id", nullable = false)
  var creatorUserId: String,

  @Column(name = "creator_project_id", nullable = false)
  var creatorProjectId: Long,

  // coming from project

  @Column(name = "created_project_id", nullable = false)
  var createdProjectId: Long,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ProjectCreationKeyDbm) return false

    if (creatorUserId != other.creatorUserId) return false
    if (creatorProjectId != other.creatorProjectId) return false
    if (createdProjectId != other.createdProjectId) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = creatorUserId.hashCode()
    result = 31 * result + creatorProjectId.hashCode()
    result = 31 * result + createdProjectId.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "ProjectCreationKeyDbm(" +
      "creatorUserId=$creatorUserId, " +
      "creatorProjectId=$creatorProjectId, " +
      "createdProjectId=$createdProjectId" +
      ")"
  }

}
