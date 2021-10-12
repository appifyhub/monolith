package com.appifyhub.monolith.storage.model.creator

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "property")
class PropertyDbm(

  @EmbeddedId
  var id: PropertyIdDbm,

  @MapsId("projectId")
  @JoinColumn(referencedColumnName = "projectId", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.EAGER)
  val project: ProjectDbm,

  @Column(nullable = false, length = 1024)
  val rawValue: String,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PropertyDbm) return false

    if (id != other.id) return false
    if (project != other.project) return false
    if (rawValue != other.rawValue) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + project.hashCode()
    result = 31 * result + rawValue.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "PropertyDbm(" +
      "id=$id, " +
      "project=$project, " +
      "value='$rawValue', " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
