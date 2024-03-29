package com.appifyhub.monolith.features.creator.storage.model

import com.appifyhub.monolith.features.user.storage.model.UserDbm
import java.io.Serializable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.MapsId

@Entity(name = "project_creation")
class ProjectCreationDbm(

  @EmbeddedId
  var data: ProjectCreationKeyDbm,

  @MapsId("id")
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumns(
    JoinColumn(name = "creator_project_id", nullable = false),
    JoinColumn(name = "creator_user_id", nullable = false),
  )
  var user: UserDbm,

  @MapsId("projectId")
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "created_project_id", nullable = false)
  var project: ProjectDbm,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ProjectCreationDbm) return false

    if (data != other.data) return false
    if (user != other.user) return false
    if (project != other.project) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = data.hashCode()
    result = 31 * result + user.hashCode()
    result = 31 * result + project.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "ProjectCreationDbm(" +
      "data=$data, " +
      "user=$user, " +
      "project=$project" +
      ")"
  }

}
